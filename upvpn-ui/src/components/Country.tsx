import React from "react";
import { Location } from "../lib/types";

import City from "./City";

type Props = {
  country_code: string;
  locations: Location[];
  enabled: boolean;
};

function Country({ country_code, locations, enabled }: Props) {
  return (
    <div>
      <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
        {locations[0].country}
      </div>
      <ul className="menu bg-base-200 border border-base-300 shadow-sm p-1 gap-1 rounded-box">
        {locations.map((loc) => {
          return (
            <li key={loc.code}>
              <City location={loc} enabled={enabled} />
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export default Country;
