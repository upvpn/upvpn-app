//
//  PasswordField.swift
//  UpVPN
//
//  Created by Himanshu on 10/8/24.
//

import SwiftUI

struct PasswordField: View {
    @Binding var password: String
    @State private var showPassword: Bool = false

    var body: some View {
        HStack {
            if showPassword {
                TextField("Password", text: $password)
                    .autocorrectionDisabled(true)
                #if os(iOS)
                    .textInputAutocapitalization(.never)
                #endif
            } else {
                SecureField("Password", text: $password)
            }

            Button(action: {
                showPassword.toggle()
            }) {
                Image(systemName: showPassword ? "eye" : "eye.slash")
                    .foregroundColor(.gray)
            }
        }
        .padding(12)
        .textFieldStyle(PlainTextFieldStyle())
        .background(RoundedRectangle(cornerRadius: 9)
            .strokeBorder(Color.gray, lineWidth: 1)
        )
    }
}
