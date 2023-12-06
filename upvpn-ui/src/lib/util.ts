import { NavigateFunction } from "react-router-dom";
import { VpnStatus, Location, UiError, Code } from "./types";
import toast from "react-hot-toast";
import { error as logError, info } from "tauri-plugin-log-api";
import { type } from '@tauri-apps/api/os';
import { invoke } from "@tauri-apps/api";
import { isPermissionGranted, requestPermission } from "@tauri-apps/api/notification";
import { KeyboardEvent } from "react";

export function getLocationFromVpnStatus(status: VpnStatus, locations: Location[]): Location | undefined {

    const locationInner = () => {
        switch (status.type) {
            case "Accepted":
            case "Connecting":
            case "Disconnecting":
            case "ServerRunning":
            case "ServerReady":
                return status.payload
            case "Connected":
                return status.payload[0]
            default:
                return undefined;
        }
    }

    var location = locationInner();
    const found = locations?.find((l) => l.code == location?.code)
    if (found !== undefined && location !== undefined) {
        location.estimate = found.estimate;
    }

    return location
}

export const isVpnInProgress = (vpnStatus: VpnStatus | undefined) => {
    if (vpnStatus === undefined) {
        return false;
    }

    switch (vpnStatus.type) {
        case "Accepted":
        case "Connecting":
        case "Connected":
        case "Disconnecting":
        case "ServerRunning":
        case "ServerReady":
        case "ServerCreated":
            return true;

        default:
            break;
    }

    return false;
};

export const isOffline = (error: UiError): boolean => {
    if (error.type === "DaemonIsOffline") {
        return true;
    }
    return false;
}

export const isUnauthenticated = (error: UiError): boolean => {
    if (error.type === "Grpc" && error.code === Code.Unauthenticated) {
        return true;
    }
    return false;
}


export const handleError = (error: UiError, navigate: NavigateFunction, isSignInPage: boolean = false) => {
    switch (error.type) {
        case "DaemonIsOffline":
            navigate("/daemon-offline");
            break;
        case "Grpc":
            logError(`code: ${error.code}, type: ${error.type},  message: ${error.message}`);
            if (isSignInPage) {
                toast.error(error.message)
            } else {
                // any other page if it receives unauthenticated then we should sign out and
                // redirect to sign page but if sign out itself errors then we just toast.error
                if (error.code == Code.Unauthenticated) {
                    // sign out
                    try {
                        const signOut = async () => {
                            await invoke("sign_out");
                        }

                        signOut();
                        navigate("/sign-in");
                    } catch (e) {
                        // nothing much to do, toast original error message
                        logError(`error ${e} occurred when handling sign out after unauthenticated`);
                        toast.error(error.message)
                    }
                } else {
                    toast.error(error.message)
                }
            }
    }
}

export const send_desktop_notification = async (message: string): Promise<boolean> => {
    const osType = await type();
    switch (osType) {
        case "Linux":
            await invoke("send_desktop_notification", {
                title: "upvpn",
                body: message,
            })
            return true;
        case "Darwin":
            let permissionGranted = await isPermissionGranted();
            info(`permissionGranted: ${permissionGranted}`);
            if (!permissionGranted) {
                const permission = await requestPermission();
                info(`permission: ${permission}`);
                permissionGranted = permission === "granted";
            }
            if (permissionGranted) {
                await invoke("send_desktop_notification", {
                    title: "upvpn",
                    body: message,
                });
                return true;
            }
            break;
        case "Windows_NT":
            break;
        default:
            break;
    }
    return false
}

export const defaultLocation = (locations: Location[]): undefined | Location => {
    return locations.find((value) => {
        return value.city.includes("Ashburn") || value.city.includes("Hillsboro");
    })
}

export const handleEnterKey = (func: () => void) => {
    const handleEnterKeyInternal = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key == "Enter") {
            func()
        }
    }

    return handleEnterKeyInternal
}
