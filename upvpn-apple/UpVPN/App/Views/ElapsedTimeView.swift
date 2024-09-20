//
//  ElapsedTimeVIew.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import SwiftUI

struct ElapsedTimeView: View {
    let startDate: Date
    @State private var elapsedTime: String = "00:00:00"
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {

        Text(elapsedTime)
            .font(.headline.weight(.semibold))
            .padding(.horizontal)
            .padding(.vertical, 1)
            .background(
                Capsule().stroke()
            )
            .frame(maxHeight: 40)
            .onReceive(timer) { _ in
                updateElapsedTime()
            }
            // minimumScaleFactor to prevent ... (dots) in displayed text
            .minimumScaleFactor(0.5)
    }

    private func updateElapsedTime() {
        let elapsed = Int(Date().timeIntervalSince(startDate))
        let hours = elapsed / 3600
        let minutes = (elapsed % 3600) / 60
        let seconds = elapsed % 60

        elapsedTime = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
    }
}

#Preview {
    ElapsedTimeView(startDate: Date.now)
}
