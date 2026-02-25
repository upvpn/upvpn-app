import React, { useContext, useEffect, useState } from "react";
import Layout from "../components/Layout";
import { useNavigate } from "react-router";
import Spinner from "../components/Spinner";
import { invoke } from "@tauri-apps/api/core";
import VpnStatusContext, {
  VpnStatusContextInterface,
} from "../context/VpnStatusContext";
import { handleEnterKey, handleError, isVpnInProgress } from "../lib/util";
import { UiError } from "../lib/types";
import { toast } from "react-hot-toast";
import Navbar from "../components/Navbar";
import { MdKeyboardArrowRight, MdOpenInNew } from "react-icons/md";

type Props = {};

function Settings({}: Props) {
  const [signingOut, setSigningOut] = useState(false);
  const [appVersion, setAppVersion] = useState("");
  const [updateAvailable, setUpdateAvailable] = useState(false);

  const { vpnStatus } = useContext(
    VpnStatusContext
  ) as VpnStatusContextInterface;

  const navigate = useNavigate();

  const inProgress = isVpnInProgress(vpnStatus);

  const onClick = () => {
    if (inProgress) {
      toast.error!("Cannot sign out when VPN session is in progress");
      return;
    }

    setSigningOut(true);
    const signOut = async () => {
      try {
        await invoke("sign_out");
        navigate("/sign-in");
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
      setSigningOut(false);
    };
    signOut();
  };

  useEffect(() => {
    const fetchVersion = async () => {
      try {
        const currentVersion = await invoke<string>("current_app_version");
        setAppVersion(currentVersion);
      } catch (e) {}
    };

    fetchVersion();
  }, []);

  useEffect(() => {
    const isUpdateAvailable = async () => {
      try {
        const isAvailable = await invoke<boolean>("update_available");
        setUpdateAvailable(isAvailable);
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
    };
    isUpdateAvailable();
  }, []);

  return (
    <Layout activeSettings={true}>
      <div className="flex flex-col h-full">
        <Navbar header="Account" />
        <div className="mx-2 flex flex-col gap-4">
          {/* Account Section */}
          <div>
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              Account
            </div>
            <ul className="menu bg-base-100 p-1 gap-1 rounded-box">
              <li>
                <a
                  href={`${import.meta.env.UPVPN_URL}/dashboard`}
                  target="_blank"
                  className="flex flex-row justify-between"
                  tabIndex={0}
                >
                  <span>Dashboard</span>
                  <MdOpenInNew size="1.5em" />
                </a>
              </li>
              <li>
                <div
                  className="flex flex-row justify-between"
                  tabIndex={0}
                  onClick={() => navigate("/help")}
                  onKeyDown={handleEnterKey(() => navigate("/help"))}
                >
                  <span>Help</span>
                  <MdKeyboardArrowRight size="1.5em" />
                </div>
              </li>
              <li className={`${inProgress || signingOut ? "disabled" : ""}`}>
                <div
                  onClick={onClick}
                  tabIndex={0}
                  onKeyDown={handleEnterKey(onClick)}
                >
                  <div className="flex flex-row items-center gap-2">
                    <Spinner
                      className={`h-6 aspect-square ${
                        signingOut ? "block" : "hidden"
                      }`}
                    />
                    <span>{signingOut ? "Signing Out" : "Sign Out"}</span>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>

        {/* Version at bottom */}
        <div className="flex-1 mb-5">
          <div className="flex flex-col gap-2 h-full justify-end">
            <a
              className={`self-center btn btn-ghost btn-wide gap-2 ${
                updateAvailable ? "" : "hidden"
              }`}
              href={`${import.meta.env.UPVPN_URL}/download`}
              target="_blank"
            >
              <p>Update available</p>
              <MdOpenInNew size="1.5em" />
            </a>
            <div
              className={`self-center badge badge-lg text-info ${
                appVersion.length > 0 ? "" : "hidden"
              }`}
              tabIndex={0}
            >
              Version: {appVersion}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Settings;
