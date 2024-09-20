//
//  AccountView.swift
//  UpVPN
//
//  Created by Himanshu on 8/16/24.
//

import SwiftUI

struct AccountView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    var body: some View {
        if let email = authViewModel.signedInUserEmail {
            Text("\(email)")
        } else {
            Text("Unauthenticated")
        }
    }
}

#Preview {
    AccountView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return false }))
}
