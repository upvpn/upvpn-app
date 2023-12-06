import {
  ReactNode,
  createContext,
  useContext,
  useEffect,
  useReducer,
  useState,
} from "react";
import { Location, VpnStatus, UiError } from "../lib/types";
import VpnStatusReducer, {
  VpnStatusAction,
  VpnStatusActionKind,
  VpnStatusState,
} from "./VpnStatusReducer";
import { listen } from "@tauri-apps/api/event";
import { invoke } from "@tauri-apps/api";
import { info } from "tauri-plugin-log-api";
import { useNavigate } from "react-router-dom";
import { handleError } from "../lib/util";

export interface ElapsedTime {
  days: string;
  hours: string;
  minutes: string;
  seconds: string;
}

export interface VpnStatusContextInterface {
  vpnStatus: VpnStatus;
  connect: (location: Location) => void;
  disconnect: (location: Location) => void;
  getVpnStatus: () => void;
}

let VpnStatusContext = createContext<VpnStatusContextInterface | undefined>(
  undefined
);

export const VpnStatusProvider = ({ children }: { children: ReactNode }) => {
  const initialVpnStatus: VpnStatus = {
    type: "Disconnected",
  };

  const initialState: VpnStatusState = {
    loading: false,
    vpnStatus: initialVpnStatus,
  };

  const navigate = useNavigate();
  const [state, dispatch] = useReducer(VpnStatusReducer, initialState);

  const connect = (location: Location) => {
    const callConnect = async () => {
      dispatch({ type: VpnStatusActionKind.Connect, payload: location });
      try {
        let vpn_status: VpnStatus = await invoke<VpnStatus>("connect", {
          location: location,
        });
        info(`connect: ${location.code}`);
        dispatch({
          type: VpnStatusActionKind.ConnectResponse,
          payload: vpn_status,
        });
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
    };
    callConnect();
  };

  const disconnect = (location: Location) => {
    const callDisconnect = async () => {
      dispatch({ type: VpnStatusActionKind.Disconnect, payload: location });
      try {
        let vpn_status: VpnStatus = await invoke<VpnStatus>("disconnect");
        info(`disconnect: ${location.code}`);
        dispatch({
          type: VpnStatusActionKind.DisconnectResponse,
          payload: vpn_status,
        });
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
    };
    callDisconnect();
  };

  const getVpnStatus = () => {
    const callGetVpnStatus = async () => {
      dispatch({ type: VpnStatusActionKind.GetVpnStatus, payload: undefined });
      try {
        let vpn_status: VpnStatus = await invoke<VpnStatus>("get_vpn_status");
        info(`getVpnStatus: ${vpn_status.type}`);
        dispatch({
          type: VpnStatusActionKind.GetVpnStatusResponse,
          payload: vpn_status,
        });
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
    };
    callGetVpnStatus();
  };

  useEffect((): (() => void) => {
    const subscribe = async () => {
      info("Subscribed to vpn status events");
      // subscribe to vpn status events
      return await listen<VpnStatus>("vpn_status", (event) => {
        const action: VpnStatusAction = {
          type: VpnStatusActionKind.VpnStatusEvent,
          payload: event.payload,
        };
        dispatch(action);
      });
    };

    const unlisten = subscribe();

    const unsubscribe = async () => {
      info("unsubscribed from vpn status events");
      (await unlisten)();
    };

    return () => {
      //console.log("Unsubscribed to vpn status events");
      // unsubscribe to vpn status events
      unsubscribe();
    };
  }, []);

  return (
    <VpnStatusContext.Provider
      value={{
        vpnStatus: state.vpnStatus,
        connect,
        disconnect,
        getVpnStatus,
      }}
    >
      {children}
    </VpnStatusContext.Provider>
  );
};

export default VpnStatusContext;
