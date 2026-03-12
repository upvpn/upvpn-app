import React, { useEffect, useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import { useNavigate } from "react-router-dom";
import Spinner from "../components/Spinner";
import { info } from "@tauri-apps/plugin-log";
import { handleError } from "../lib/util";
import { UiError } from "../lib/types";
import useAuthStatus from "../hooks/useAuthStatus";
import LogoIcon from "../components/LogoIcon";

const SignIn = () => {
  const navigate = useNavigate();
  const [checkingAuthStatus, daemonOffline, signedIn] = useAuthStatus();

  const [checking, setChecking] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [googleSignInChecking, setGoogleSignInChecking] = useState(false);

  useEffect(() => {
    if (import.meta.env.PROD) {
      document.addEventListener("contextmenu", (event) =>
        event.preventDefault()
      );
    }
  }, []);

  useEffect(() => {
    if (signedIn) {
      navigate("/");
    }
  }, [signedIn]);

  const onSubmit = (e: React.SyntheticEvent) => {
    e.preventDefault();
    const signIn = async () => {
      setChecking(true);
      try {
        info("signing in ...");
        const success = await invoke("sign_in", {
          email,
          password,
        });
        navigate("/");
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate, true);
      }
      setChecking(false);
    };

    signIn();
  };

  const onGoogleSignIn = () => {
    const googleSignIn = async () => {
      setGoogleSignInChecking(true);
      try {
        info("signing in with Google ...");
        await invoke("google_sign_in");
        navigate("/");
      } catch (e) {
        const error = e as UiError;
        handleError(error, navigate, true);
      }
      setGoogleSignInChecking(false);
    };

    googleSignIn();
  };

  const onCancelGoogleSignIn = () => {
    const cancelGoogleSignIn = async () => {
      try {
        await invoke("cancel_google_sign_in");
      } catch (e) {
        // ignore cancel errors
      }
    };

    cancelGoogleSignIn();
  };

  return (
    <div className="min-h-screen bg-base-200 select-none">
      <div className="hero-content flex-col lg:flex-row-reverse">
        <LogoIcon />
        <div className="text-center lg:text-left">
          <h2 className="text-5xl">UpVPN</h2>
          <p className="py-4 font-bold text-base-content/50">A Modern Serverless VPN</p>
        </div>
        <div className="card flex-shrink-0 w-full max-w-sm shadow-2xl bg-base-100">
          <div className="p-4">
            <form onSubmit={onSubmit}>
              <div className="form-control">
                <label htmlFor="email" className="label">
                  <span className="label-text">Email</span>
                </label>

                <input
                  name="email"
                  type="email"
                  autoCorrect="off"
                  autoCapitalize="none"
                  autoComplete="email"
                  onChange={(e) => setEmail(e.target.value)}
                  className="input input-bordered focus:ring-1 focus:outline-none hover:ring-1"
                  disabled={checking || googleSignInChecking}
                  required
                  autoFocus={true}
                />
              </div>
              <div className="form-control">
                <label htmlFor="password" className="label">
                  <span className="label-text">Password</span>
                </label>
                <input
                  name="password"
                  type="password"
                  id="current-password"
                  autoComplete="current-password"
                  onChange={(e) => setPassword(e.target.value)}
                  className="input input-bordered focus:ring-1 focus:outline-none hover:ring-1"
                  disabled={checking || googleSignInChecking}
                  required
                />
                <label className="label">
                  <a
                    href={`${import.meta.env.UPVPN_URL}/sign-up`}
                    target="_blank"
                    className="label-text-alt link link-hover"
                  >
                    Need an account
                  </a>
                  <a
                    href={`${import.meta.env.UPVPN_URL}/forgot-password`}
                    target="_blank"
                    className="label-text-alt link link-hover"
                  >
                    Forgot password
                  </a>
                </label>
              </div>
              <div className="form-control mt-6">
                <div className="flex gap-2">
                  <button className="btn btn-primary flex-1" disabled={checking || googleSignInChecking}>
                    {checking ? <Spinner className="w-12 h-12" /> : <>Sign In</>}
                  </button>
                  {googleSignInChecking ? (
                    <button
                      className="btn btn-outline btn-error flex-1"
                      type="button"
                      onClick={onCancelGoogleSignIn}
                    >
                      <div className="flex items-center gap-2">
                        <Spinner className="w-5 h-5" />
                        <div>Cancel</div>
                      </div>
                    </button>
                  ) : (
                    <button
                      className="btn btn-outline flex-1 gap-1"
                      type="button"
                      onClick={onGoogleSignIn}
                      disabled={checking}
                    >
                      <div className="flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" className="w-5 h-5">
                          <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
                          <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
                          <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
                          <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
                        </svg>
                        <div>Google</div>
                      </div>
                    </button>
                  )}
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
