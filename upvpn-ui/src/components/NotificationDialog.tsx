import { useContext } from "react";
import { DateTime } from "luxon";
import { invoke } from "@tauri-apps/api/core";
import { useLocation, useNavigate } from "react-router-dom";
import NotificationContext, {
  NotificationContextInterface,
} from "../context/NotificationContext";
import AccountContext, {
  AccountContextInterface,
} from "../context/AccountContext";

// Shows one dialog at a time for daemon notifications.
// On OK the notification is acked, and for insufficient balance
// user is taken to the plan page.
const NotificationDialog = () => {
  const { notifications, ackNotification } = useContext(
    NotificationContext
  ) as NotificationContextInterface;
  const { clearAccount } = useContext(
    AccountContext
  ) as AccountContextInterface;
  const navigate = useNavigate();
  const location = useLocation();

  if (location.pathname === "/sign-in" || location.pathname === "/daemon-offline") {
    return null;
  }

  if (notifications.length === 0) {
    return null;
  }

  const notification = notifications[0];

  const onOk = () => {
    ackNotification(notification.id);

    const message = notification.message.toLowerCase();

    if (message.includes("insufficient balance")) {
      navigate("/plan");
      return;
    }

    // device token is no longer valid: sign out to clear daemon
    // state and let user sign in again
    if (message.includes("unauthenticated")) {
      const signOut = async () => {
        try {
          await invoke("sign_out");
        } catch (e) {}
        clearAccount();
        navigate("/sign-in");
      };
      signOut();
    }
  };

  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <h3 className="font-bold text-lg text-info">Oh No</h3>
        <p className="py-4 max-h-32 overflow-y-auto">{notification.message}</p>
        <p className="text-xs text-accent font-bold">
          {DateTime.fromISO(notification.timestamp).toLocaleString(
            DateTime.DATETIME_MED
          )}
        </p>
        <div className="modal-action">
          <button className="btn" onClick={onOk}>
            OK
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotificationDialog;
