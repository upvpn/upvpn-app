import React from "react";
import Layout from "../components/Layout";
import Navbar from "../components/Navbar";
import { invoke } from "@tauri-apps/api/core";
import { MdCircle } from "react-icons/md";
import { handleEnterKey } from "../lib/util";
import { type as getOsType } from "@tauri-apps/plugin-os";

function Help() {
  const isLinux = getOsType() === "linux";

  const showOSSLicenses = () => {
    invoke("open_license");
  };

  const showLogFile = () => {
    invoke("open_log_file");
  };

  return (
    <Layout activeSettings={true}>
      <div className="flex flex-col h-full">
        <Navbar header="Help" />
        <div className="mx-2 flex flex-col gap-4 overflow-y-auto pb-4 flex-1">
          {/* Color indicators */}
          <div>
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              What are color indicators?
            </div>
            <div className="bg-base-100 rounded-box p-4 flex flex-col gap-3">
              <div className="flex items-center gap-3">
                <MdCircle className="text-green-600 shrink-0" size="0.75em" />
                <span className="text-sm">Connect quickly to available servers</span>
              </div>
              <div className="flex items-center gap-3">
                <MdCircle className="text-sky-400 shrink-0" size="0.75em" />
                <span className="text-sm">Create and connect to a new server</span>
              </div>
            </div>
          </div>

          {/* System Tray - Linux only */}
          {isLinux && (
            <div>
              <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
                System Tray
              </div>
              <div className="bg-base-100 rounded-box p-4 text-sm leading-relaxed">
                <p>
                  Install the {" "}
                  <a
                    href="https://extensions.gnome.org/extension/615/appindicator-support/"
                    target="_blank"
                    className="link"
                  >
                    AppIndicator
                  </a>{" "}
                   extension on GNOME to use system tray.
                </p>
              </div>
            </div>
          )}

          {/* FAQ / Support */}
          <div>
            <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
              Questions about product or pricing?
            </div>
            <div className="bg-base-100 rounded-box p-4 text-sm leading-relaxed">
              <p>
                Visit{" "}
                <a href="https://upvpn.app/faq/" target="_blank" className="link">
                  FAQ
                </a>
                {" "}
                or email us at support@upvpn.app and we'll be happy to assist!
              </p>

            </div>
          </div>

          {/* Bottom links */}
          <div className="flex-1" />
          <div className="pb-2 px-4 flex flex-row justify-between">
            <span
              className="text-xs text-base-content/50 cursor-pointer hover:text-base-content/80"
              onClick={showLogFile}
              tabIndex={0}
              onKeyDown={handleEnterKey(showLogFile)}
            >
              View logs
            </span>
            <span
              className="text-xs text-base-content/50 cursor-pointer hover:text-base-content/80"
              onClick={showOSSLicenses}
              tabIndex={0}
              onKeyDown={handleEnterKey(showOSSLicenses)}
            >
              Acknowledgements
            </span>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Help;
