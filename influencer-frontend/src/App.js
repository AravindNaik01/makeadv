import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import {
  Routes,
  Route,
  Navigate,
  useNavigate,
  useLocation,
  useSearchParams,
} from "react-router-dom";
import "./App.css";

const API_BASE = process.env.REACT_APP_API_BASE || "http://127.0.0.1:8080";

function OAuthInstagramCallback({ setToken, navigate }) {
  const [searchParams] = useSearchParams();
  useEffect(() => {
    const err = searchParams.get("error");
    const t = searchParams.get("token");
    if (err) {
      navigate("/login?error=" + encodeURIComponent(err), { replace: true });
      return;
    }
    if (t) {
      setToken(t);
      navigate("/influencer", { replace: true });
      return;
    }
    navigate("/login", { replace: true });
  }, [searchParams, setToken, navigate]);
  return (
    <div className="authPage">
      <p className="oauthWait">Completing Instagram sign-in…</p>
    </div>
  );
}

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const isRegisterPath = location.pathname === "/register";

  const [role, setRole] = useState("INFLUENCER");
  const [loginAs, setLoginAs] = useState("BUSINESS");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState("");
  const [token, setToken] = useState("");
  const [influencers, setInfluencers] = useState([]);
  const [loadingInfluencers, setLoadingInfluencers] = useState(false);
  const [infError, setInfError] = useState("");
  const [sendingTo, setSendingTo] = useState("");
  const [sendError, setSendError] = useState("");

  const [requests, setRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [reqError, setReqError] = useState("");
  const [actingOn, setActingOn] = useState(0);

  const influencerOnlyAuth =
    (!isRegisterPath && loginAs === "INFLUENCER") ||
    (isRegisterPath && role === "INFLUENCER");

  const client = useMemo(() => {
    const instance = axios.create({ baseURL: API_BASE });
    return instance;
  }, []);

  const authedClient = useMemo(() => {
    const instance = axios.create({ baseURL: API_BASE });
    instance.interceptors.request.use((config) => {
      const t = token?.trim();
      if (t) config.headers.Authorization = `Bearer ${t}`;
      return config;
    });
    return instance;
  }, [token]);

  useEffect(() => {
    const p = new URLSearchParams(location.search);
    const err = p.get("error");
    if (err && (location.pathname === "/login" || location.pathname === "/register")) {
      setStatus(decodeURIComponent(err.replace(/\+/g, " ")));
    }
  }, [location.search, location.pathname]);

  async function startInstagramSignIn() {
    setStatus("Redirecting to Instagram…");
    try {
      const res = await client.get("/auth/instagram/url");
      if (res.data?.url) {
        window.location.href = res.data.url;
        return;
      }
      const msg = [res.data?.error, res.data?.hint].filter(Boolean).join(" ");
      setStatus(msg || "Could not start Instagram sign-in.");
    } catch (e) {
      const d = e.response?.data;
      const msg =
        d && typeof d === "object"
          ? [d.hint, d.error].filter(Boolean).join(" ")
          : e.message;
      setStatus(msg || "Could not reach server. Is the backend running?");
    }
  }

  async function loadInfluencers() {
    setLoadingInfluencers(true);
    setInfError("");
    try {
      const res = await authedClient.get("/influencers");
      setInfluencers(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setInfluencers([]);
      setInfError(
        err?.response?.data?.message ||
          err?.response?.data ||
          err.message ||
          "Could not load influencers"
      );
    } finally {
      setLoadingInfluencers(false);
    }
  }

  async function sendRequestTo(influencerUsername) {
    const infU = (influencerUsername || "").trim();
    if (!infU) return;
    if (!token?.trim()) {
      setSendError("Not logged in — please log in again as Business.");
      return;
    }
    setSendingTo(infU);
    setSendError("");
    try {
      const res = await authedClient.post("/request", { influencerUsername: infU });
      const msg =
        typeof res.data === "string"
          ? res.data.trim()
          : String(res.data ?? "");
      if (!msg.toLowerCase().includes("request sent")) {
        setSendError(msg || "Server did not confirm the request.");
        return;
      }
      setStatus(`Request sent to @${infU}`);
    } catch (err) {
      const data = err?.response?.data;
      setSendError(
        (typeof data === "string" ? data : data?.message) ||
          err?.response?.statusText ||
          err.message ||
          "Could not send request"
      );
    } finally {
      setSendingTo("");
    }
  }

  async function loadMyRequests() {
    setLoadingRequests(true);
    setReqError("");
    try {
      const res = await authedClient.get("/requests/influencer");
      setRequests(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setRequests([]);
      setReqError(
        err?.response?.data?.message ||
          err?.response?.data ||
          err.message ||
          "Could not load requests"
      );
    } finally {
      setLoadingRequests(false);
    }
  }

  async function actOnRequest(id, action) {
    if (!id) return;
    setActingOn(id);
    setReqError("");
    try {
      const path = action === "accept" ? `/accept/${id}` : `/reject/${id}`;
      await authedClient.put(path);
      await loadMyRequests();
      setStatus(action === "accept" ? "Request accepted" : "Request rejected");
    } catch (err) {
      setReqError(
        err?.response?.data?.message ||
          err?.response?.data ||
          err.message ||
          "Action failed"
      );
    } finally {
      setActingOn(0);
    }
  }

  async function submitAuth(e) {
    e.preventDefault();
    if (influencerOnlyAuth) {
      return;
    }
    setStatus("Working...");
    try {
      const apiPath = isRegisterPath ? "/register" : "/login";
      const payload = isRegisterPath
        ? { username, password, role }
        : { username, password };
      const res = await client.post(apiPath, payload);

      const body = res?.data;
      if (typeof body === "string") {
        setStatus(body);
        if (!isRegisterPath && body.includes(".")) {
          setToken(body);
          if (loginAs === "BUSINESS") {
            navigate("/business", { replace: true });
          }
        }
      } else {
        setStatus("OK");
      }
    } catch (err) {
      setStatus(err?.response?.data?.message || err.message || "Request failed");
    }
  }

  useEffect(() => {
    if (location.pathname !== "/business") return;
    if (!token) return;
    loadInfluencers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname, token]);

  useEffect(() => {
    if (location.pathname !== "/influencer") return;
    if (!token) return;
    loadMyRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname, token]);

  function logout() {
    setToken("");
    setInfluencers([]);
    setInfError("");
    setRequests([]);
    setReqError("");
    setSendError("");
    setStatus("");
    setPassword("");
    navigate("/login", { replace: true });
  }

  const authPage = (
    <div className="authPage">
      <div className="authShell">
        <aside className="authBrand">
          <div className="brandMark" aria-hidden="true">
            <div className="brandOrb" />
            <div className="brandOrb two" />
          </div>
          <h1>Influencer Platform</h1>
          <p className="sub">
            Connect businesses with verified influencers. Sign in to continue.
          </p>
        </aside>

        <main className="authCard" aria-label="Authentication">
          <div className="tabs">
            <button
              className={!isRegisterPath ? "active" : ""}
              onClick={() => {
                navigate("/login");
                setStatus("");
              }}
              type="button"
            >
              Login
            </button>
            <button
              className={isRegisterPath ? "active" : ""}
              onClick={() => {
                navigate("/register");
                setStatus("");
              }}
              type="button"
            >
              Register
            </button>
          </div>

          {!isRegisterPath ? (
            <div className="rolePick" aria-label="Login as">
              <button
                type="button"
                className={loginAs === "BUSINESS" ? "role active" : "role"}
                onClick={() => setLoginAs("BUSINESS")}
              >
                Business
              </button>
              <button
                type="button"
                className={loginAs === "INFLUENCER" ? "role active" : "role"}
                onClick={() => setLoginAs("INFLUENCER")}
              >
                Influencer
              </button>
            </div>
          ) : (
            <div className="rolePick" aria-label="Register as">
              <button
                type="button"
                className={role === "BUSINESS" ? "role active" : "role"}
                onClick={() => setRole("BUSINESS")}
              >
                Business
              </button>
              <button
                type="button"
                className={role === "INFLUENCER" ? "role active" : "role"}
                onClick={() => setRole("INFLUENCER")}
              >
                Influencer
              </button>
            </div>
          )}

          <h2 className="authTitle">
            {influencerOnlyAuth
              ? isRegisterPath
                ? "Join with Instagram"
                : "Sign in with Instagram"
              : !isRegisterPath
                ? "Welcome back"
                : "Create your account"}
          </h2>
          <p className="muted authHint">
            {influencerOnlyAuth
              ? "You must authenticate securely with an actual Instagram account."
              : !isRegisterPath
                ? `Logging in as ${loginAs.toLowerCase()}.`
                : `Registering as ${role.toLowerCase()}.`}
          </p>
          {influencerOnlyAuth ? (
            <div className="igAuthBlock">
              <button type="button" className="igButton" onClick={startInstagramSignIn}>
                Continue with Instagram
              </button>
              <p className="muted igFinePrint">
                Ensure your <code>instagram.client-id</code> and{" "}
                <code>instagram.client-secret</code> are properly configured in <code>application.properties</code> for the OAuth redirect to work correctly.
              </p>
            </div>
          ) : (
            <form onSubmit={submitAuth} className="form">
              <label>
                <span>Username</span>
                <input
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                  placeholder="e.g. aravind"
                />
              </label>
              <label>
                <span>Password</span>
                <input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  type="password"
                  autoComplete={isRegisterPath ? "new-password" : "current-password"}
                  placeholder="••••••••"
                />
              </label>
              <button type="submit" className="primary">
                {!isRegisterPath ? "Login" : "Create account"}
              </button>
            </form>
          )}

          <p className="status">
            <b>Status:</b> {status || "—"}
          </p>
        </main>
      </div>
    </div>
  );

  const businessPage = (
    <div className="dashPage">
      <header className="dashHeader">
        <div>
          <div className="dashKicker">Business</div>
          <h1 className="dashTitle">Influencers</h1>
          <p className="muted">Browse and shortlist influencers for your campaigns.</p>
        </div>
        <div className="dashActions">
          <button type="button" className="ghost" onClick={loadInfluencers}>
            {loadingInfluencers ? "Loading..." : "Refresh"}
          </button>
          <button type="button" className="ghost" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <main className="dashBody">
        {infError ? <div className="errorBox">{String(infError)}</div> : null}
        {sendError ? <div className="errorBox">{String(sendError)}</div> : null}

        <div className="tableWrap" role="region" aria-label="Influencers table">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Location</th>
                <th className="num">Trust</th>
                <th className="num">Followers</th>
                <th className="num">Posts</th>
                <th className="num">Likes</th>
                <th className="num">Comments</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {influencers.length === 0 ? (
                <tr>
                  <td colSpan={9} className="emptyCell">
                    {loadingInfluencers ? "Loading..." : "No influencers found."}
                  </td>
                </tr>
              ) : (
                influencers.map((inf, idx) => (
                  <tr key={`${inf?.username ?? "inf"}-${idx}`}>
                    <td className="nameCell">
                      <div className="nameMain">{inf?.name || "Unnamed"}</div>
                      {inf?.username ? (
                        <div className="nameSub">@{inf.username}</div>
                      ) : null}
                    </td>
                    <td>{inf?.category || "—"}</td>
                    <td>{inf?.location || "—"}</td>
                    <td className="num">
                      <span className="trustPill">{inf?.trustScore ?? 0}</span>
                    </td>
                    <td className="num">{inf?.followers ?? 0}</td>
                    <td className="num">{inf?.posts ?? 0}</td>
                    <td className="num">{inf?.likes ?? 0}</td>
                    <td className="num">{inf?.comments ?? 0}</td>
                    <td className="num">
                      <button
                        type="button"
                        className="ghost"
                        disabled={!inf?.username || sendingTo === inf.username}
                        onClick={() => sendRequestTo(inf.username)}
                      >
                        {sendingTo === inf.username ? "Sending..." : "Request"}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="cards" aria-label="Influencers cards">
          {influencers.map((inf, idx) => (
            <article className="infCard" key={`card-${inf?.username ?? "inf"}-${idx}`}>
              <div className="infTop">
                <div>
                  <div className="infName">{inf?.name || "Unnamed"}</div>
                  <div className="infHandle">
                    {inf?.username ? `@${inf.username}` : "—"}
                  </div>
                </div>
                <div className="trustPill big">{inf?.trustScore ?? 0}</div>
              </div>
              <div className="infTags">
                <span>{inf?.category || "—"}</span>
                <span>{inf?.location || "—"}</span>
              </div>
              <div className="infStats">
                <div>
                  <div className="statNum">{inf?.followers ?? 0}</div>
                  <div className="statLbl">Followers</div>
                </div>
                <div>
                  <div className="statNum">{inf?.posts ?? 0}</div>
                  <div className="statLbl">Posts</div>
                </div>
                <div>
                  <div className="statNum">{inf?.likes ?? 0}</div>
                  <div className="statLbl">Likes</div>
                </div>
                <div>
                  <div className="statNum">{inf?.comments ?? 0}</div>
                  <div className="statLbl">Comments</div>
                </div>
              </div>

              <div className="cardActions">
                <button
                  type="button"
                  className="primary"
                  disabled={!inf?.username || sendingTo === inf.username}
                  onClick={() => sendRequestTo(inf.username)}
                >
                  {sendingTo === inf.username ? "Sending..." : "Send request"}
                </button>
              </div>
            </article>
          ))}
        </div>
      </main>
    </div>
  );

  const influencerPage = (
    <div className="dashPage">
      <header className="dashHeader">
        <div>
          <div className="dashKicker">Influencer</div>
          <h1 className="dashTitle">Requests</h1>
          <p className="muted">Review business requests to advertise with you.</p>
        </div>
        <div className="dashActions">
          <button type="button" className="ghost" onClick={loadMyRequests}>
            {loadingRequests ? "Loading..." : "Refresh"}
          </button>
          <button type="button" className="ghost" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <main className="dashBody">
        {reqError ? <div className="errorBox">{String(reqError)}</div> : null}

        <div className="reqList" aria-label="Incoming requests">
          {requests.length === 0 ? (
            <div className="emptyBox">
              {loadingRequests ? "Loading..." : "No requests yet."}
            </div>
          ) : (
            requests.map((r) => (
              <div className="reqCard" key={r.id}>
                <div>
                  <div className="reqTitle">
                    From <b>{r.businessName || "Business"}</b>
                  </div>
                  <div className="reqMeta">
                    <span className={`badge ${String(r.status || "").toLowerCase()}`}>
                      {r.status || "—"}
                    </span>
                    <span className="mono">#{r.id}</span>
                  </div>
                </div>
                <div className="reqActions">
                  <button
                    type="button"
                    className="ghost"
                    disabled={String(r.status).toUpperCase() !== "PENDING" || actingOn === r.id}
                    onClick={() => actOnRequest(r.id, "reject")}
                  >
                    {actingOn === r.id ? "..." : "Reject"}
                  </button>
                  <button
                    type="button"
                    className="primary"
                    disabled={String(r.status).toUpperCase() !== "PENDING" || actingOn === r.id}
                    onClick={() => actOnRequest(r.id, "accept")}
                  >
                    {actingOn === r.id ? "..." : "Accept"}
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </main>
    </div>
  );

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={authPage} />
      <Route path="/register" element={authPage} />
      <Route
        path="/oauth/instagram"
        element={<OAuthInstagramCallback setToken={setToken} navigate={navigate} />}
      />
      <Route
        path="/business"
        element={token ? businessPage : <Navigate to="/login" replace />}
      />
      <Route
        path="/influencer"
        element={token ? influencerPage : <Navigate to="/login" replace />}
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
