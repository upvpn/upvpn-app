#!/usr/bin/env bash

set -ueo pipefail

LOG_DIR="/var/log/upvpn"
UPVPN_DAEMON_PLIST_PATH="/Library/LaunchDaemons/app.upvpn.daemon.plist"

ask_confirmation() {
    read -p "Are you sure you want to stop and uninstall upvpn? (y/n) "
    if [[ "$REPLY" =~ [Yy]$ ]]; then
        echo "Uninstalling upvpn ..."
    else
        echo "Thank you for keeping upvpn"
        exit 0
    fi
}

stop_daemon_and_ui() {
    echo "Stopping and unloading upvpn-daemon ..."
    if [[ -f "${UPVPN_DAEMON_PLIST_PATH}" ]]; then
        # Stop Daemon and UI
        sudo launchctl unload -w "${UPVPN_DAEMON_PLIST_PATH}" || true
        sudo pkill -x "upvpn-ui" || true
        sudo rm "${UPVPN_DAEMON_PLIST_PATH}" || true
    fi
}

uninstall() {
    echo "Removing files ..."
    sudo rm /usr/local/bin/upvpn || true
    sudo rm -rf /Applications/upvpn.app || true
    sudo pkgutil --forget app.upvpn.macos || true
    sudo rm -rf /var/log/upvpn || true
    sudo rm -rf /etc/upvpn || true
}

main() {
    ask_confirmation
    stop_daemon_and_ui
    uninstall
    echo "Done."
}

main "$@"
