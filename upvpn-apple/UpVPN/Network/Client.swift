//
//  Client.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//

import Foundation

import Foundation

import Foundation

struct ApiError: Decodable, Error {
    let errorType: String
    let message: String

    func isUnauthorized() -> Bool {
        self.errorType == "unauthorized"
    }
}

extension ApiError : CustomStringConvertible {
    var description: String {
        return self.message
    }
}

enum ClientError: Error {
    case invalidURL
    case networkError(Error)
    case decodingError(Error)
    case unexpectedResponse
    case apiError(ApiError)
}

func mapClientError(clientError: ClientError) -> ApiError {
    return switch clientError {
    case .invalidURL:
        ApiError(errorType: "invalidURL", message: "invalid URL")
    case .networkError(let error):
        ApiError(errorType: "networkError", message: error.localizedDescription)
    case .decodingError(let error):
        ApiError(errorType: "decodingError", message: error.localizedDescription)
    case .unexpectedResponse:
        ApiError(errorType: "unexpectedResponse", message: "unexpected response")
    case .apiError(let apiError):
        apiError
    }
}

struct RequestOptions {
    let maxRetries: Int
    let retryDelay: TimeInterval

    static let `default` = RequestOptions(maxRetries: 0, retryDelay: 1.0)
}

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
}

class Client {
    private let baseURL: URL
    private let session: URLSession
    private let getAuthToken: () async -> String?
    private let defaultOptions: RequestOptions

    init(baseURL: URL,
         trustAllCertificates: Bool = false,
         getAuthToken: @escaping () async -> String?,
         defaultOptions: RequestOptions = .default)  {

        self.baseURL = baseURL
        self.getAuthToken = getAuthToken
        self.defaultOptions = defaultOptions

        let configuration = URLSessionConfiguration.default
        if trustAllCertificates {
            configuration.urlCache = nil
            configuration.requestCachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        }
        // defautl timeout seems to be 1min
        configuration.timeoutIntervalForRequest = 10

        self.session = URLSession(configuration: configuration)
    }

    func request<T: Decodable>(_ path: String,
                               method: HTTPMethod = .get,
                               body: Data? = nil,
                               options: RequestOptions? = nil) async -> Result<T, ClientError> {
        let requestOptions = options ?? defaultOptions
        return await withRetry(options: requestOptions) { [weak self] in
            guard let self = self else {
                return .failure(.unexpectedResponse)
            }

            guard let url = URL(string: path, relativeTo: self.baseURL) else {
                return .failure(.invalidURL)
            }

            var request = URLRequest(url: url)
            request.httpMethod = method.rawValue
            request.httpBody = body
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")

            if let token = await self.getAuthToken() {
                request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
            }


            do {
                let (data, response) = try await self.session.data(for: request)

                guard let httpResponse = response as? HTTPURLResponse else {
                    return .failure(.unexpectedResponse)
                }

                if (200...299).contains(httpResponse.statusCode) {
                    // handle empty response
                    if data.isEmpty && T.self == Empty.self {
                        return .success(Empty() as! T)
                    } else {
                        let decodedResponse = try JSONDecoder().decode(T.self, from: data)
                        return .success(decodedResponse)
                    }
                } else {

                    if let apiError = try? JSONDecoder().decode(ApiError.self, from: data) {
                        return .failure(.apiError(apiError))
                    } else {
                        return .failure(.unexpectedResponse)
                    }
                }
            } catch let error as DecodingError {
                return .failure(.decodingError(error))
            } catch {
                return .failure(.networkError(error))
            }
        }
    }

    private func withRetry<T>(options: RequestOptions, attempt: Int = 0, operation: @escaping () async -> Result<T, ClientError>) async -> Result<T, ClientError> {
        let result = await operation()

        switch result {
        case .success:
            return result
        case .failure(let error):
            switch error {
            case .networkError, .unexpectedResponse:
                if attempt < options.maxRetries {
                    try? await Task.sleep(nanoseconds: UInt64(options.retryDelay * 1_000_000_000))
                    return await withRetry(options: options, attempt: attempt + 1, operation: operation)
                } else {
                    print("max retries exceeded")
                    return .failure(error)
                }
            default:
                return result
            }
        }
    }
}
