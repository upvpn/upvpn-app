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
import AccountContext, {
  AccountContextInterface,
} from "../context/AccountContext";
import { toast } from "react-hot-toast";
import Navbar from "../components/Navbar";
import { MdKeyboardArrowRight, MdOpenInNew } from "react-icons/md";

type Props = {};

function Settings({}: Props) {
  const [signingOut, setSigningOut] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [appVersion, setAppVersion] = useState("");
  const [updateAvailable, setUpdateAvailable] = useState(false);

  const { vpnStatus } = useContext(
    VpnStatusContext
  ) as VpnStatusContextInterface;

  const { email, emailLoaded, getAccountInfo, clearAccount } = useContext(
    AccountContext
  ) as AccountContextInterface;

  const navigate = useNavigate();

  const inProgress = isVpnInProgress(vpnStatus);

  const onClick = () => {
    if (inProgress) {
      toast.error!("Cannot sign out when VPN session is in progress");
      return;
    }
    setShowConfirm(true);
  };

  const confirmSignOut = () => {
    setShowConfirm(false);
    setSigningOut(true);
    const signOut = async () => {
      try {
        await invoke("sign_out");
        clearAccount();
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
    getAccountInfo();
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
        <div className="mx-2 flex flex-col gap-3">
          {/* Profile Section */}
          <div>
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              Profile
            </div>
            <ul className="menu bg-base-200 border border-base-300 shadow-sm p-1 gap-1 rounded-box overflow-hidden">
              {(email.length > 0 || !emailLoaded) && (
                <li className="pointer-events-none">
                  <div className="flex flex-row justify-between">
                    <span>Email</span>
                    {email.length > 0 ? (
                      <span className="opacity-70 whitespace-nowrap text-xs">
                        {email}
                      </span>
                    ) : (
                      <span className="animate-pulse bg-base-300 rounded h-4 w-36"></span>
                    )}
                  </div>
                </li>
              )}
              <li>
                <div
                  className="flex flex-row justify-between"
                  tabIndex={0}
                  onClick={() => navigate("/plan")}
                  onKeyDown={handleEnterKey(() => navigate("/plan"))}
                >
                  <span>Plan</span>
                  <MdKeyboardArrowRight size="1.5em" />
                </div>
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

          {/* Referrals Section */}
          <div>
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              Referrals
            </div>
            <ul className="menu bg-base-200 border border-base-300 shadow-sm p-1 gap-1 rounded-box">
              <li>
                <div
                  className="flex flex-row justify-between"
                  tabIndex={0}
                  onClick={() => navigate("/refer")}
                  onKeyDown={handleEnterKey(() => navigate("/refer"))}
                >
                  <span>Refer a friend</span>
                  <MdKeyboardArrowRight size="1.5em" />
                </div>
              </li>
            </ul>
          </div>

          {/* Version Section */}
          <div className="mb-5">
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              Version
            </div>
            <ul className="menu bg-base-200 border border-base-300 shadow-sm p-1 gap-1 rounded-box">
              <li className={updateAvailable ? "" : "pointer-events-none"}>
                {updateAvailable ? (
                  <a
                    href={`${import.meta.env.UPVPN_URL}/download`}
                    target="_blank"
                    className="flex flex-row justify-between"
                    tabIndex={0}
                  >
                    <span className="opacity-70">{appVersion}</span>
                    <span className="flex flex-row items-center gap-2 text-info">
                      <span>Update available</span>
                      <MdOpenInNew size="1.5em" />
                    </span>
                  </a>
                ) : (
                  <div className="flex flex-row justify-between">
                    <span className="opacity-70">{appVersion}</span>
                  </div>
                )}
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div className={`modal${showConfirm ? " modal-open" : ""}`}>
        <div className="modal-box">
          <h3 className="font-bold text-lg">Sign Out</h3>
          <p className="py-4">Are you sure you want to sign out?</p>
          <div className="modal-action">
            <button
              className="btn"
              onClick={() => setShowConfirm(false)}
            >
              Cancel
            </button>
            <button
              className="btn btn-error"
              onClick={confirmSignOut}
            >
              Sign Out
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Settings;
