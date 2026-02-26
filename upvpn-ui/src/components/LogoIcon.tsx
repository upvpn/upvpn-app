import React from "react";
import { ReactComponent as Logo } from "../assets/upvpn.svg";

const LogoIcon = () => (
  <div className="bg-white dark:bg-black rounded-xl p-2">
    <Logo className="w-12 h-12 text-black dark:text-white" />
  </div>
);

export default LogoIcon;
