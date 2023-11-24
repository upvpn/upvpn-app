import React from "react";
import City from "./City";
import { Location } from "../lib/types";
import { ReactComponent as Logo } from "../assets/upvpn.svg";

type Props = {
  locations: Location[];
  disabled: boolean;
};

const RecentLocations = ({ locations, disabled }: Props) => {
  if (locations.length == 0) {
    return (
      <div className="card  bg-base-300 h-40">
        <ul className="menu h-full my-2 p-2 rounded-box">
          <div className="flex flex-col h-full w-full items-center justify-center">
            <Logo className="w-12 h-12" />
          </div>
        </ul>
      </div>
    );
  }

  const cities = locations.map((location) => {
    return (
      <li key={location.code}>
        <City location={location} key={location.code} enabled={!disabled} />
      </li>
    );
  });

  return (
    <div className="card  bg-base-300 h-40 overflow-y-auto">
      <ul className="menu my-2 p-2 rounded-box">
        <li className="menu-title">
          <div>Recent Locations</div>
        </li>
        {cities}
      </ul>
    </div>
  );
};

export default RecentLocations;
