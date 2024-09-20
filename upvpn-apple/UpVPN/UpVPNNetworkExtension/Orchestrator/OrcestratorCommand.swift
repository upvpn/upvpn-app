//
//  OrchestratorCommand.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//

import Foundation

protocol OrchestratorCommand {
    associatedtype Response
    func execute(in actor: VPNOrchestrator) async -> Response
}

protocol AnyCommandBox {
    func execute(in actor: VPNOrchestrator) async
}

struct CommandBox<T: OrchestratorCommand>: AnyCommandBox {
    let command: T
    let continuation: CheckedContinuation<T.Response, Never>

    func execute(in actor: VPNOrchestrator) async {
        let response = await command.execute(in: actor)
        continuation.resume(returning: response)
    }
}


struct Start: OrchestratorCommand {
    typealias Response = Result<(), OrchestratorError>

    var location: Location

    init(location: Location) {
        self.location = location
    }

    func execute(in actor: VPNOrchestrator) async -> Result<(), OrchestratorError> {
        await actor.start(location: self.location)
    }
}


struct Stop: OrchestratorCommand {
    typealias Response = Result<(), OrchestratorError>

    let reason: String

    init(reason: String) {
        self.reason = reason
    }

    func execute(in actor: VPNOrchestrator) async -> Result<(), OrchestratorError> {
        await actor.stop(reason: self.reason)
    }
}

struct ServerStatusUpdate: OrchestratorCommand {
    typealias Response = Void

    let newStatus: VpnSessionStatus
    let location: Location

    init(newStatus: VpnSessionStatus, location: Location) {
        self.newStatus = newStatus
        self.location = location
    }

    func execute(in actor: VPNOrchestrator) async -> Void {
        await actor.onServerStatusUpdate(newStatus: self.newStatus, location: self.location)
    }
}
