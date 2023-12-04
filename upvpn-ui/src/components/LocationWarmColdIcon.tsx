import React, { useContext } from "react";
import {
  MdKeyboardArrowRight,
  MdOutlineWbSunny,
  MdOutlineAcUnit,
} from "react-icons/md";
import { Location } from "../lib/types";
import VpnStatusContext, {
  VpnStatusContextInterface,
} from "../context/VpnStatusContext";
import { isVpnInProgress } from "../lib/util";

type Props = {
  location: Location;
  arrow: boolean;
};

const LocationWarmColdIcon = ({ location, arrow }: Props) => {
  const { vpnStatus } = useContext(
    VpnStatusContext
  ) as VpnStatusContextInterface;

  if (isVpnInProgress(vpnStatus)) {
    if (arrow) {
      return <MdKeyboardArrowRight size="1.5em" />;
    } else {
      <></>;
    }
  }

  if (location.estimate === undefined) {
    if (arrow) {
      return <MdKeyboardArrowRight size="1.5em" />;
    } else {
      return <></>;
    }
  } else {
    if (location.estimate <= 10) {
      return (
        <MdOutlineWbSunny
          size="1.2em"
          className="mx-1 dark:text-success text-green-600"
        />
      );
    } else {
      return (
        <MdOutlineAcUnit
          size="1.2em"
          className="mx-1 dark:text-info text-sky-400"
        />
      );
    }
  }
};

export default LocationWarmColdIcon;
