import { useContext } from "react";
import { DateTime } from "luxon";
import { useLocation, useNavigate } from "react-router-dom";
import NotificationContext, {
  NotificationContextInterface,
} from "../context/NotificationContext";

// Shows one dialog at a time for daemon notifications.
// On OK the notification is acked, and for insufficient balance
// user is taken to the plan page.
const NotificationDialog = () => {
  const { notifications, ackNotification } = useContext(
    NotificationContext
  ) as NotificationContextInterface;
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
    if (notification.message.toLowerCase().includes("insufficient balance")) {
      navigate("/plan");
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
