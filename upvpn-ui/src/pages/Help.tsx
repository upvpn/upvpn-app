import React from "react";
import Layout from "../components/Layout";
import Navbar from "../components/Navbar";
import { invoke } from "@tauri-apps/api/core";
import { MdCircle, MdOpenInNew } from "react-icons/md";
import { handleEnterKey } from "../lib/util";

function Help() {
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
              </p>
              <p className="mt-2">
                Or email us at support@upvpn.app and we'll be happy to assist!
              </p>
            </div>
          </div>

          {/* Logs */}
          <ul className="menu bg-base-100 p-1 gap-1 rounded-box">
            <li onClick={showLogFile}>
              <div
                className="flex flex-row justify-between"
                tabIndex={0}
                onKeyDown={handleEnterKey(showLogFile)}
              >
                <span>View Logs</span>
                <MdOpenInNew size="1.5em" />
              </div>
            </li>
          </ul>

          {/* Acknowledgements at bottom */}
          <div className="flex-1" />
          <div className="pb-2 text-center">
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
