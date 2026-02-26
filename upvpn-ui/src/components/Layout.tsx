import React, { useEffect } from "react";
import {
  MdOutlineHome,
  MdOutlineLocationOn,
  MdOutlineManageAccounts,
} from "react-icons/md";
import { useNavigate } from "react-router-dom";

type Props = {
  activeHome?: boolean;
  activeLocation?: boolean;
  activeSettings?: boolean;
  children?: React.ReactNode;
};

function Layout(props: Props) {
  const navigate = useNavigate();

  useEffect(() => {
    if (import.meta.env.PROD) {
      document.addEventListener("contextmenu", (event) =>
        event.preventDefault()
      );
    }
  }, []);

  return (
    <div className="flex flex-col h-screen">
      <div className="flex-1 min-h-0 overflow-y-auto scroll-smooth">
        {props.children}
      </div>
      <div className="flex flex-row justify-around items-center w-full h-16 bg-base-200 flex-shrink-0">
        <button
          className={`flex flex-col items-center justify-center h-full flex-1 text-info ${props.activeLocation ? "border-t-2 border-current bg-base-100" : ""}`}
          onClick={() => navigate("/locations")}
        >
          <MdOutlineLocationOn size="1.5em" />
        </button>
        <button
          className={`flex flex-col items-center justify-center h-full flex-1 text-info ${props.activeHome ? "border-t-2 border-current bg-base-100" : ""}`}
          onClick={() => navigate("/")}
        >
          <MdOutlineHome size="1.5em" />
        </button>
        <button
          className={`flex flex-col items-center justify-center h-full flex-1 text-info ${props.activeSettings ? "border-t-2 border-current bg-base-100" : ""}`}
          onClick={() => navigate("/settings")}
        >
          <MdOutlineManageAccounts size="1.5em" />
        </button>
      </div>
    </div>
  );
}

export default Layout;
