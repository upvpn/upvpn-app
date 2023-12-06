import React, { useContext } from "react";
import { Location } from "../lib/types";
import ReactCountryFlag from "react-country-flag";
import LocationContext, {
  LocationContextInterface,
} from "../context/LocationContext";
import { toast } from "react-hot-toast";
import { handleEnterKey } from "../lib/util";
import LocationWarmColdIcon from "./LocationWarmColdIcon";

type Props = {
  location: Location;
  enabled: boolean;
};

function City({ location, enabled }: Props) {
  const { selected, setLocation } = useContext(
    LocationContext
  ) as LocationContextInterface;

  const onClick = () => {
    if (enabled === true) {
      setLocation(location);
    } else {
      toast.error("Cannot change location when vpn session is in progress");
    }
  };

  return (
    <div
      className="flex justify-between"
      onClick={onClick}
      onKeyDown={handleEnterKey(onClick)}
      tabIndex={0}
    >
      <div className="flex gap-2 items-center">
        <ReactCountryFlag
          className="rounded"
          svg
          countryCode={location.country_code}
          style={{
            width: "1.5em",
          }}
        />
        <div className="font-bold">{location.city}</div>
      </div>
      <div className="flex flex-row gap-2 items-center">
        <LocationWarmColdIcon location={location} arrow={false} />
        <input
          type="radio"
          className="radio"
          checked={(selected && selected.code === location.code) || false}
          onChange={() => {}}
          disabled={enabled ? false : true}
        />
      </div>
    </div>
  );
}

export default City;
