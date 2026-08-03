import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { invoke } from "@tauri-apps/api/core";
import { toast } from "react-hot-toast";
import { MdRefresh, MdStar } from "react-icons/md";
import Layout from "../components/Layout";
import Navbar from "../components/Navbar";
import Spinner from "../components/Spinner";
import { PurchasePlan, UiError, UserPlan } from "../lib/types";
import { handleEnterKey, handleError } from "../lib/util";

// same fixed plans as Android and iOS apps
const PREPAID_AMOUNTS_CENTS = [499, 999, 1499, 2499];
const YEARLY_PRICE_CENTS = 3999;

const dollars = (cents: number): string => {
  return `$${(cents / 100).toFixed(2)}`;
};

const isSamePlan = (a: PurchasePlan | undefined, b: PurchasePlan): boolean => {
  if (a === undefined) {
    return false;
  }
  if (a.type === "PayAsYouGo" && b.type === "PayAsYouGo") {
    return a.content === b.content;
  }
  return a.type === b.type;
};

type PriceCapsuleProps = {
  price: string;
  isSelected: boolean;
  onClick: () => void;
};

const PriceCapsule = ({ price, isSelected, onClick }: PriceCapsuleProps) => {
  return (
    <button
      className={`btn btn-sm rounded-full font-semibold text-info bg-base-100 hover:bg-base-100 ${
        isSelected
          ? "border-2 border-info hover:border-info"
          : "border-2 border-transparent hover:border-transparent"
      }`}
      onClick={onClick}
    >
      {price}
    </button>
  );
};

type CurrentPlanProps = {
  userPlan: UserPlan;
};

const CurrentPlan = ({ userPlan }: CurrentPlanProps) => {
  return (
    <div>
      <div className="text-xs font-semibold text-base-content/50 uppercase tracking-wider px-4 pb-1">
        Current Plan
      </div>
      <div className="card bg-base-100 rounded-box">
        <div className="card-body px-4 py-3">
          {userPlan.type === "PayAsYouGo" && (
            <div className="flex flex-row justify-between">
              <span>Pay as you go</span>
              <span>Balance {dollars(userPlan.content.balance)}</span>
            </div>
          )}
          {userPlan.type !== "PayAsYouGo" && (
            <div className="flex flex-col gap-2">
              <span>Yearly</span>
              <div className="divider my-0"></div>
              <div className="flex flex-row items-center gap-3 text-info">
                <MdStar size="1.5em" />
                <span className="text-base-content">
                  You're on the best plan
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

function Plan() {
  const navigate = useNavigate();
  const [userPlan, setUserPlan] = useState<UserPlan | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [errored, setErrored] = useState(false);
  const [selected, setSelected] = useState<PurchasePlan | undefined>(undefined);
  const [purchasing, setPurchasing] = useState(false);

  const loadUserPlan = async () => {
    setErrored(false);
    try {
      const plan = await invoke<UserPlan>("user_plan");
      setUserPlan(plan);
    } catch (e) {
      setErrored(true);
      const error = e as UiError;
      handleError(error, navigate);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadUserPlan();

    // reload when window regains focus: user may have
    // completed purchase in browser
    const onFocus = () => {
      loadUserPlan();
    };
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, []);

  const onBuy = () => {
    if (selected === undefined || purchasing) {
      return;
    }
    setPurchasing(true);
    const buy = async () => {
      try {
        await invoke("checkout", { purchasePlan: selected });
        toast.success("Complete your purchase in the browser");
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate);
      }
      setPurchasing(false);
    };
    buy();
  };

  const buyButtonText = () => {
    if (selected === undefined) {
      return "Buy Now";
    }
    if (selected.type === "PayAsYouGo") {
      return `Add ${dollars(selected.content)} Credit`;
    }
    return `Buy Yearly ${dollars(YEARLY_PRICE_CENTS)}/year`;
  };

  return (
    <Layout>
      <div className="flex flex-col h-full">
        <Navbar header="Plan" />

        {loading && (
          <div className="flex-1 flex items-center justify-center">
            <Spinner className="h-6 aspect-square" />
          </div>
        )}

        {!loading && errored && (
          <div className="flex-1 flex items-center justify-center">
            <div
              className="flex flex-row items-center gap-3 cursor-pointer"
              tabIndex={0}
              onClick={loadUserPlan}
              onKeyDown={handleEnterKey(loadUserPlan)}
            >
              <span>Couldn't load plan, retry</span>
              <MdRefresh size="1.5em" />
            </div>
          </div>
        )}

        {!loading && !errored && userPlan !== undefined && (
          <div className="flex flex-col flex-1 min-h-0">
            <div className="flex-1 overflow-y-auto mx-2 flex flex-col gap-4 pb-4">
              <CurrentPlan userPlan={userPlan} />

              {userPlan.type === "PayAsYouGo" && (
                <>
                  <div className="card bg-base-100 rounded-box">
                    <div className="card-body px-4 py-3 gap-1">
                      <h2 className="font-semibold">Prepaid Credit</h2>
                      <p className="text-sm opacity-70">
                        Add to Pay-as-you-go balance
                      </p>
                      <div className="divider my-1"></div>
                      <div className="grid grid-cols-2 gap-4 justify-items-center py-2">
                        {PREPAID_AMOUNTS_CENTS.map((amountCents) => (
                          <PriceCapsule
                            key={amountCents}
                            price={dollars(amountCents)}
                            isSelected={isSamePlan(selected, {
                              type: "PayAsYouGo",
                              content: amountCents,
                            })}
                            onClick={() =>
                              setSelected({
                                type: "PayAsYouGo",
                                content: amountCents,
                              })
                            }
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  <div
                    className="card bg-base-100 rounded-box cursor-pointer"
                    onClick={() => setSelected({ type: "AnnualSubscription" })}
                  >
                    <div className="card-body px-4 py-3">
                      <div className="flex flex-row justify-between items-center">
                        <div className="flex flex-col gap-1">
                          <h2 className="font-semibold">Yearly Plan</h2>
                          <p className="text-sm opacity-70">
                            Get unlimited data
                          </p>
                        </div>
                        <PriceCapsule
                          price={`${dollars(YEARLY_PRICE_CENTS)}/year`}
                          isSelected={isSamePlan(selected, {
                            type: "AnnualSubscription",
                          })}
                          onClick={() =>
                            setSelected({ type: "AnnualSubscription" })
                          }
                        />
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>

            {userPlan.type === "PayAsYouGo" && (
              <div className="flex flex-col gap-2 px-4 pb-4">
                <button
                  className="btn btn-info w-full"
                  disabled={selected === undefined || purchasing}
                  onClick={onBuy}
                >
                  {purchasing ? (
                    <Spinner className="h-6 aspect-square" />
                  ) : (
                    buyButtonText()
                  )}
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </Layout>
  );
}

export default Plan;
