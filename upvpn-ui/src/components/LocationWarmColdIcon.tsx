import React, { useContext } from "react";
import { MdKeyboardArrowRight, MdCircle } from "react-icons/md";
import { Location } from "../lib/types";
import VpnStatusContext, {
  VpnStatusContextInterface,
} from "../context/VpnStatusContext";
import { isVpnInProgress } from "../lib/util";
import Spinner from "./Spinner";

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
      // session is transitioning (not connected yet, or disconnecting)
      if (vpnStatus !== undefined && vpnStatus.type !== "Connected") {
        return <Spinner className="h-6 w-6" />;
      }
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
      return <MdCircle size="1em" className="mx-1 text-green-600 " />;
    } else {
      return <MdCircle size="1em" className="mx-1 text-sky-400 " />;
    }
  }
};

export default LocationWarmColdIcon;
