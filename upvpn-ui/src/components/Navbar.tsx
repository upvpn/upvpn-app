import React from "react";
import { MdKeyboardArrowLeft } from "react-icons/md";
import { useNavigate } from "react-router-dom";
import { handleEnterKey } from "../lib/util";

type Props = {
  header: string;
};

const Navbar = ({ header }: Props) => {
  const navigate = useNavigate();

  return (
    <div className="navbar">
      <div className="navbar-start">
        <div
          tabIndex={0}
          className="btn btn-square btn-ghost"
          onClick={() => navigate(-1)}
          onKeyDown={handleEnterKey(() => {
            navigate(-1);
          })}
        >
          <MdKeyboardArrowLeft size="2em" />
        </div>
      </div>
      <div className="navbar-center">
        <div className="text font-bold">{header}</div>
      </div>
      <div className="navbar-end"></div>
    </div>
  );
};

export default Navbar;
