import { ReactNode, createContext, useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import { AccountInfo } from "../lib/types";

export interface AccountContextInterface {
  email: string;
  emailLoaded: boolean;
  getAccountInfo: () => void;
  clearAccount: () => void;
}

const AccountContext = createContext<AccountContextInterface | undefined>(
  undefined
);

export const AccountProvider = ({ children }: { children: ReactNode }) => {
  const [email, setEmail] = useState("");
  const [emailLoaded, setEmailLoaded] = useState(false);

  const getAccountInfo = () => {
    // already loaded for current sign in
    if (emailLoaded) {
      return;
    }

    const fetchAccountInfo = async () => {
      try {
        const accountInfo = await invoke<AccountInfo>("account_info");
        setEmail(accountInfo.email);
        setEmailLoaded(true);
      } catch (e) {}
    };

    fetchAccountInfo();
  };

  const clearAccount = () => {
    setEmail("");
    setEmailLoaded(false);
  };

  return (
    <AccountContext.Provider
      value={{
        email,
        emailLoaded,
        getAccountInfo,
        clearAccount,
      }}
    >
      {children}
    </AccountContext.Provider>
  );
};

export default AccountContext;
