import { toast } from "react-hot-toast";
import { writeText } from "@tauri-apps/plugin-clipboard-manager";
import { MdContentCopy } from "react-icons/md";
import Layout from "../components/Layout";
import Navbar from "../components/Navbar";

const REFERRAL_MESSAGE =
  "Check out this cool VPN app! https://UpVPN.app\n\nUse promo code UPVPN to unlock a discount.";

function Refer() {
  const onCopy = async () => {
    try {
      await writeText(REFERRAL_MESSAGE);
      toast.success("Copied to clipboard");
    } catch (e) {
      toast.error("Couldn't copy to clipboard");
    }
  };

  return (
    <Layout>
      <div className="flex flex-col h-full">
        <Navbar header="Refer a Friend" />
        <div className="mx-3 flex flex-col gap-3">
          <div className="card bg-base-200 border border-base-300 shadow-sm rounded-box">
            <div className="card-body px-4 py-3">
              <div className="flex flex-row justify-between items-center gap-2">
                <p className="whitespace-pre-line">{REFERRAL_MESSAGE}</p>
                <button
                  className="btn btn-square btn-ghost"
                  onClick={onCopy}
                  aria-label="Copy to clipboard"
                >
                  <MdContentCopy size="1.5em" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Refer;
