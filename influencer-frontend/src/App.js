import React, { useEffect, useState, useCallback, useMemo, useRef } from "react";
import axios from "axios";
import {
  Routes,
  Route,
  Navigate,
  useNavigate,
  useLocation,
} from "react-router-dom";
import "./App.css";

const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080";
const WS_BASE  = (process.env.REACT_APP_API_BASE || "http://localhost:8080");

function NotificationCenter({ notifications, unreadCount, open, onToggle, onMarkAllRead, onClearAll }) {
  return (
    <div className="notif-wrap">
      <button className="notif-btn" onClick={onToggle} title="Notifications">
        <span>🔔</span>
        {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
      </button>

      {open && (
        <div className="notif-dropdown">
          <div className="notif-head">
            <strong>Notifications</strong>
            <span>{unreadCount > 0 ? `${unreadCount} unread` : "All caught up"}</span>
          </div>
          <div className="notif-actions">
            <button type="button" onClick={onMarkAllRead}>Mark all read</button>
            <button type="button" onClick={onClearAll}>Clear all</button>
          </div>
          <div className="notif-list">
            {notifications.length === 0 && (
              <div className="notif-empty">No notifications yet.</div>
            )}
            {notifications.map((n) => (
              <div key={n.id} className={`notif-item ${n.read ? "" : "unread"}`}>
                <div className="notif-msg">{n.message}</div>
                <div className="notif-time">{new Date(n.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   HOME PAGE  (no state, pure presentational)
───────────────────────────────────────────── */
function HomePage() {
  const navigate = useNavigate();
  return (
    <div className="home">
      <div className="orb orb-1" />
      <div className="orb orb-2" />
      <div className="orb orb-3" />

      <nav className="home-nav">
        <div className="home-nav-logo">
          <span className="logo-icon">⚡</span> MakeAdv
        </div>
        <div className="home-nav-actions">
          <button className="btn-ghost" onClick={() => navigate("/login")}>Login</button>
          <button className="btn-primary" onClick={() => navigate("/register")}>Get Started</button>
        </div>
      </nav>

      <section className="hero">
        <div className="hero-badge">✦ Influencer Marketing Platform</div>
        <h1 className="hero-title">
          Connect Brands with
          <span className="gradient-text"> Verified Influencers</span>
        </h1>
        <p className="hero-sub">
          MakeAdv bridges the gap between businesses and authentic creators.
          Launch campaigns, track performance, and grow together.
        </p>
        <div className="hero-cta">
          <button className="btn-primary large" onClick={() => navigate("/register")}>
            Start for Free →
          </button>
          <button className="btn-outline large" onClick={() => navigate("/login")}>
            Sign In
          </button>
        </div>
        <div className="stats-row">
          <div className="stat-item"><div className="stat-num">10K+</div><div className="stat-lbl">Influencers</div></div>
          <div className="stat-divider" />
          <div className="stat-item"><div className="stat-num">2K+</div><div className="stat-lbl">Brands</div></div>
          <div className="stat-divider" />
          <div className="stat-item"><div className="stat-num">50K+</div><div className="stat-lbl">Campaigns</div></div>
        </div>
      </section>

      <section className="role-section">
        <div className="role-card" onClick={() => navigate("/register?role=BUSINESS")}>
          <div className="role-card-icon">🏢</div>
          <h3>For Businesses</h3>
          <p>Discover verified influencers, send collaboration requests, and track campaign results in real time.</p>
          <span className="role-card-cta">Register as Business →</span>
        </div>
        <div className="role-card influencer-card" onClick={() => navigate("/register?role=INFLUENCER")}>
          <div className="role-card-icon">🌟</div>
          <h3>For Influencers</h3>
          <p>Showcase your reach, manage incoming brand requests, and grow your collaborations effortlessly.</p>
          <span className="role-card-cta">Register as Influencer →</span>
        </div>
      </section>

      <footer className="home-footer">© 2025 MakeAdv · Built with ❤️</footer>
    </div>
  );
}

/* ─────────────────────────────────────────────
   AUTH PAGE  — role-first, no toggle confusion
───────────────────────────────────────────── */
function AuthPage({ mode, onLogin }) {
  const navigate = useNavigate();
  const location = useLocation();
  const isRegister = mode === "register";

  const presetRole = new URLSearchParams(location.search).get("role");
  // null means "not chosen yet" — show role picker screen
  const [role, setRole] = useState(
    presetRole === "BUSINESS" ? "BUSINESS" : presetRole === "INFLUENCER" ? "INFLUENCER" : null
  );
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  // Reset form when mode (login/register) changes
  useEffect(() => {
    setUsername(""); setPassword(""); setStatus(""); setLoading(false);
  }, [mode]);

  // ── STEP 1: Role picker ──────────────────────────────────────────
  if (!role) {
    return (
      <div className="auth-page">
        <div className="orb orb-1" /><div className="orb orb-2" />
        <button className="back-btn" onClick={() => navigate("/")}>← Home</button>

        <div className="role-picker-shell">
          <div className="role-picker-logo"><span className="logo-icon">⚡</span> MakeAdv</div>
          <h2 className="role-picker-title">Who are you?</h2>
          <p className="role-picker-sub">Choose your account type to {isRegister ? "get started" : "sign in"}</p>

          <div className="role-picker-cards">
            <button className="rpc biz" onClick={() => setRole("BUSINESS")}>
              <span className="rpc-emoji">🏢</span>
              <span className="rpc-name">Business</span>
              <span className="rpc-desc">Find &amp; connect with influencers for your campaigns</span>
              <span className="rpc-arrow">→</span>
            </button>
            <button className="rpc inf" onClick={() => setRole("INFLUENCER")}>
              <span className="rpc-emoji">🌟</span>
              <span className="rpc-name">Influencer</span>
              <span className="rpc-desc">Receive brand deals and grow your collaborations</span>
              <span className="rpc-arrow">→</span>
            </button>
          </div>

          <p className="auth-switch center">
            {isRegister ? "Already have an account?" : "Don't have an account?"}{" "}
            <button type="button" className="link-btn"
              onClick={() => navigate(isRegister ? "/login" : "/register")}>
              {isRegister ? "Sign in" : "Create one"}
            </button>
          </p>
        </div>
      </div>
    );
  }

  // ── STEP 2: Form (role already chosen) ───────────────────────────
  const isBusiness = role === "BUSINESS";
  const roleLabel = isBusiness ? "Business" : "Influencer";
  const roleEmoji = isBusiness ? "🏢" : "🌟";
  const roleAccent = isBusiness ? "biz" : "inf";

  async function handleSubmit(e) {
    e.preventDefault();
    if (!username.trim() || !password.trim()) { setStatus("Please fill in all fields."); return; }
    setLoading(true); setStatus("");
    try {
      const apiPath = isRegister ? "/auth/register" : "/auth/login";
      const payload = { username: username.trim(), password, role };

      const res = await axios.post(API_BASE + apiPath, payload);
      const body = res?.data;

      if (isRegister) {
        const msg = typeof body === "string" ? body : (body?.message || "Registered!");
        if (msg.toLowerCase().includes("registered")) {
          setStatus("✅ Account created! Redirecting to login…");
          setTimeout(() => navigate("/login?role=" + role), 1200);
        } else {
          setStatus("⚠️ " + msg);
        }
      } else {
        if (body?.token) {
          onLogin(body.token, body.role, username.trim());
          navigate(body.role === "INFLUENCER" ? "/influencer" : "/business", { replace: true });
        } else {
          setStatus("⚠️ " + (body?.message || "Invalid credentials"));
        }
      }
    } catch (err) {
      setStatus("⚠️ " + (err?.response?.data?.message || err.message || "Request failed"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="orb orb-1" /><div className="orb orb-2" />
      <button className="back-btn" onClick={() => setRole(null)}>← Change role</button>

      <div className="auth-shell">
        {/* Left panel */}
        <div className={`auth-left auth-left-${roleAccent}`}>
          <div className="auth-left-logo"><span className="logo-icon">⚡</span> MakeAdv</div>
          <div className="auth-role-badge">{roleEmoji} {roleLabel} Portal</div>
          <h2 className="auth-left-title">
            {isRegister ? `Create your ${roleLabel} account` : `Welcome back, ${roleLabel}`}
          </h2>
          <p className="auth-left-sub">
            {isBusiness
              ? isRegister
                ? "Start discovering verified influencers and launching campaigns."
                : "Sign in to manage your campaigns and collaboration requests."
              : isRegister
                ? "Join and start receiving collaboration requests from top brands."
                : "Sign in to review brand requests and manage your profile."}
          </p>
          <ul className="auth-features">
            {isBusiness ? (
              <>
                <li><span className="feat-dot" />Browse verified influencers</li>
                <li><span className="feat-dot" />Send collaboration requests</li>
                <li><span className="feat-dot" />Track campaign performance</li>
              </>
            ) : (
              <>
                <li><span className="feat-dot" />Receive brand collaboration offers</li>
                <li><span className="feat-dot" />Accept or reject requests easily</li>
                <li><span className="feat-dot" />Build your trust score</li>
              </>
            )}
          </ul>
          <p className="auth-switch">
            {isRegister ? "Already have an account?" : "Don't have an account?"}{" "}
            <button type="button" className="link-btn"
              onClick={() => navigate(isRegister ? `/login?role=${role}` : `/register?role=${role}`)}>
              {isRegister ? "Sign in" : "Create one"}
            </button>
          </p>
        </div>

        {/* Right form panel */}
        <div className="auth-right">
          <div className="auth-tabs">
            <button type="button" className={!isRegister ? "tab active" : "tab"}
              onClick={() => navigate("/login?role=" + role)}>Login</button>
            <button type="button" className={isRegister ? "tab active" : "tab"}
              onClick={() => navigate("/register?role=" + role)}>Register</button>
          </div>

          {/* Static role badge — no toggle */}
          <div className={`role-indicator ${roleAccent}`}>
            <span>{roleEmoji}</span>
            <span>{isRegister ? `Registering as ${roleLabel}` : `Logging in as ${roleLabel}`}</span>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="field">
              <label htmlFor="auth-username">Username</label>
              <input id="auth-username" type="text" value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder={isBusiness ? "e.g. acme_corp" : "e.g. john_creator"}
                autoComplete="username" />
            </div>
            <div className="field">
              <label htmlFor="auth-password">Password</label>
              <div className="pass-wrap">
                <input id="auth-password" type={showPass ? "text" : "password"} value={password}
                  onChange={e => setPassword(e.target.value)} placeholder="••••••••"
                  autoComplete={isRegister ? "new-password" : "current-password"} />
                <button type="button" className="eye-btn" onClick={() => setShowPass(p => !p)} tabIndex={-1}>
                  {showPass ? "🙈" : "👁️"}
                </button>
              </div>
            </div>
            <button type="submit" className={`submit-btn submit-${roleAccent}`} disabled={loading}>
              {loading ? <span className="spinner" /> : isRegister ? `Create ${roleLabel} Account` : `Sign In as ${roleLabel}`}
            </button>
          </form>

          {status && (
            <div className={`auth-status ${status.startsWith("✅") ? "success" : "error"}`}>{status}</div>
          )}
        </div>
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   CHAT PANEL  (real-time STOMP + history REST)
───────────────────────────────────────────── */
function ChatPanel({ connectionId, myUsername, otherUsername, token, onClose }) {
  const [messages, setMessages]       = useState([]);
  const [draft, setDraft]             = useState("");
  const [loadingHist, setLoadingHist] = useState(true);
  const [connected, setConnected]     = useState(false);
  const [sending, setSending]         = useState(false);
  const [pickedFile, setPickedFile]   = useState(null);
  const [sendError, setSendError]     = useState("");

  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const textareaRef    = useRef(null);
  const fileInputRef   = useRef(null);

  // ── Scroll to bottom whenever messages change ──────────────────────
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // ── Load chat history via REST ─────────────────────────────────────
  useEffect(() => {
    if (!connectionId) return;
    setLoadingHist(true);
    axios
      .get(`${API_BASE}/chat/history/${connectionId}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => setMessages(Array.isArray(res.data) ? res.data : []))
      .catch(() => setMessages([]))
      .finally(() => setLoadingHist(false));
  }, [connectionId, token]);

  // ── Connect to STOMP WebSocket ─────────────────────────────────────
  useEffect(() => {
    if (!connectionId) return;

    let client = null;
    let cleanup = () => {};

    // Dynamically import STOMP + SockJS to avoid bundler issues
    Promise.all([
      import("@stomp/stompjs"),
      import("sockjs-client"),
    ])
      .then(([{ Client }, SockJSModule]) => {
        const SockJS = SockJSModule.default || SockJSModule;

        client = new Client({
          webSocketFactory: () => new SockJS(`${WS_BASE}/ws-chat`),
          reconnectDelay: 5000,
          onConnect: () => {
            setConnected(true);
            // Subscribe to the connection-specific topic
            client.subscribe(`/topic/chat/${connectionId}`, (frame) => {
              try {
                const msg = JSON.parse(frame.body);
                setMessages((prev) => {
                  // Avoid duplicates (REST history + WS)
                  const exists = prev.some(
                    (m) =>
                      m.sender === msg.sender &&
                      m.content === msg.content &&
                      m.sentAt === msg.sentAt
                  );
                  return exists ? prev : [...prev, msg];
                });
              } catch {}
            });
          },
          onDisconnect: () => setConnected(false),
          onStompError:  () => setConnected(false),
        });

        client.activate();
        stompClientRef.current = client;
        cleanup = () => { try { client.deactivate(); } catch {} };
      })
      .catch(() => {
        // STOMP not available — polling fallback
        const interval = setInterval(() => {
          axios
            .get(`${API_BASE}/chat/history/${connectionId}`, {
              headers: { Authorization: `Bearer ${token}` },
            })
            .then((res) => setMessages(Array.isArray(res.data) ? res.data : []))
            .catch(() => {});
        }, 3000);
        cleanup = () => clearInterval(interval);
      });

    return () => cleanup();
  }, [connectionId, token]);

  // ── Send a message ─────────────────────────────────────────────────
  const sendMessage = useCallback(() => {
    const text = draft.trim();
    if (!text && !pickedFile) return;
    setSending(true);
    setSendError("");

    const publishPayload = (payload) => {
      if (stompClientRef.current?.connected) {
        stompClientRef.current.publish({
          destination: "/app/chat.send",
          body: JSON.stringify(payload),
        });
      }
    };

    const send = async () => {
      try {
        let payload = {
          connectionId,
          sender: myUsername,
          recipient: otherUsername,
          content: text,
          messageType: "TEXT",
        };

        if (pickedFile) {
          const form = new FormData();
          form.append("file", pickedFile);
          const uploadRes = await axios.post(`${API_BASE}/chat/upload`, form, {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "multipart/form-data",
            },
          });
          const u = uploadRes.data || {};
          payload = {
            ...payload,
            content: text || (u.fileName ? `Sent ${u.fileName}` : "Attachment"),
            messageType: u.messageType || "DOCUMENT",
            fileName: u.fileName || pickedFile.name,
            fileUrl: u.fileUrl,
            fileSize: u.fileSize || pickedFile.size,
            mimeType: u.mimeType || pickedFile.type,
          };
        }

        publishPayload(payload);
        setDraft("");
        setPickedFile(null);
        if (fileInputRef.current) fileInputRef.current.value = "";
      } finally {
        setSending(false);
      }
    };

    send().catch((err) => {
      const msg = err?.response?.data?.message || err?.response?.data || err?.message || "Failed to send message";
      setSendError(String(msg));
      setSending(false);
    });
  }, [draft, pickedFile, connectionId, myUsername, otherUsername, token]);

  // ── Enter key to send (Shift+Enter for newline) ────────────────────
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // ── Format timestamp ───────────────────────────────────────────────
  const formatTime = (ts) => {
    if (!ts) return "";
    try {
      const d = new Date(ts);
      return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    } catch {
      return "";
    }
  };

  const fullFileUrl = (url) => {
    if (!url) return "";
    const withToken = (base) => `${base}${base.includes("?") ? "&" : "?"}token=${encodeURIComponent(token)}`;
    if (url.startsWith("http://") || url.startsWith("https://")) return withToken(url);
    return withToken(`${API_BASE}${url}`);
  };

  return (
    <div className="chat-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="chat-panel">
        {/* Header */}
        <div className="chat-header">
          <div className="chat-header-avatar">
            {(otherUsername || "?")[0].toUpperCase()}
          </div>
          <div className="chat-header-info">
            <div className="chat-header-name">@{otherUsername}</div>
            <div className="chat-header-status">
              {connected ? "Online · Connected" : "Connecting…"}
            </div>
          </div>
          <button className="chat-close-btn" onClick={onClose} title="Close chat">✕</button>
        </div>

        {/* Disconnected warning */}
        {!connected && !loadingHist && (
          <div className="chat-disconnected">
            ⚠ Reconnecting to real-time channel…
          </div>
        )}

        {/* Messages */}
        <div className="chat-messages">
          {loadingHist && (
            <div className="chat-loading">
              <span className="chat-dot" />
              <span className="chat-dot" />
              <span className="chat-dot" />
              <span style={{ marginLeft: 6 }}>Loading messages…</span>
            </div>
          )}

          {!loadingHist && messages.length === 0 && (
            <div className="chat-empty">
              <div className="chat-empty-icon">💬</div>
              <div>No messages yet. Say hello!</div>
            </div>
          )}

          {messages.map((msg, i) => {
            const isMine = msg.sender === myUsername;
            const fileUrl = fullFileUrl(msg.fileUrl);
            const isImage = msg.messageType === "IMAGE";
            const isVideo = msg.messageType === "VIDEO";
            const isDoc = msg.messageType === "DOCUMENT";
            return (
              <div key={msg.id ?? i} className={`chat-msg ${isMine ? "mine" : "theirs"}`}>
                {!isMine && (
                  <div className="chat-sender-name">@{msg.sender}</div>
                )}
                <div className="chat-bubble">{msg.content}</div>
                {isImage && fileUrl && (
                  <a href={fileUrl} target="_blank" rel="noreferrer" className="chat-media-link">
                    <img src={fileUrl} alt={msg.fileName || "image"} className="chat-image" />
                  </a>
                )}
                {isVideo && fileUrl && (
                  <video src={fileUrl} className="chat-video" controls />
                )}
                {isDoc && fileUrl && (
                  <a href={fileUrl} target="_blank" rel="noreferrer" className="chat-doc-link">
                    📎 {msg.fileName || "Download attachment"}
                  </a>
                )}
                <div className="chat-ts">{formatTime(msg.sentAt)}</div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>

        {/* Input */}
        <div className="chat-input-area">
          <input
            ref={fileInputRef}
            type="file"
            className="chat-file-input"
            onChange={(e) => setPickedFile(e.target.files?.[0] || null)}
          />
          <button
            className="chat-attach-btn"
            onClick={() => fileInputRef.current?.click()}
            title="Attach photo, video or document"
            type="button"
          >
            📎
          </button>
          <textarea
            ref={textareaRef}
            className="chat-input"
            rows={1}
            placeholder="Type a message… (Enter to send)"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          {pickedFile && (
            <div className="chat-file-pill" title={pickedFile.name}>
              {pickedFile.name}
            </div>
          )}
          <button
            className="chat-send-btn"
            disabled={(!draft.trim() && !pickedFile) || sending}
            onClick={sendMessage}
            title="Send message"
          >
            ➤
          </button>
        </div>
        {sendError && <div className="chat-send-error">⚠ {sendError}</div>}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   BUSINESS DASHBOARD  (defined OUTSIDE App)
───────────────────────────────────────────── */
function BusinessDash({ authed, onLogout, token, myUsername }) {
  const [tab, setTab] = useState("browse"); // "browse" | "requests" | "profile" | "chat"

  // ── Chat state ────────────────────────────────────────────────────
  const [chatConn, setChatConn] = useState(null); // { id, otherUsername }

  // ── Profile tab state ──────────────────────────────────────────────
  const [profile, setProfile] = useState({ name: "", industry: "", location: "" });
  const [profileMsg, setProfileMsg] = useState("");

  // ── Browse tab state ──────────────────────────────────────────────
  const [influencers, setInfluencers] = useState([]);
  const [loadingInf, setLoadingInf] = useState(false);
  const [infError, setInfError] = useState("");
  const [sendingTo, setSendingTo] = useState("");
  const [sendMsg, setSendMsg] = useState("");

  // ── My Requests tab state ─────────────────────────────────────────
  const [sentReqs, setSentReqs] = useState([]);
  const [loadingReqs, setLoadingReqs] = useState(false);
  const [reqsError, setReqsError] = useState("");
  const [notifications, setNotifications] = useState([]);
  const [notifOpen, setNotifOpen] = useState(false);
  const prevStatusesRef = useRef(new Map());
  const notifWsClientRef = useRef(null);
  const seenNotifMessagesRef = useRef(new Set());

  const unreadCount = useMemo(
    () => notifications.reduce((acc, n) => acc + (n.read ? 0 : 1), 0),
    [notifications]
  );

  const pushNotif = useCallback((message) => {
    setNotifications((prev) => [
      { id: `${Date.now()}-${Math.random()}`, message, createdAt: Date.now(), read: false },
      ...prev.slice(0, 29),
    ]);
  }, []);

  const loadInfluencers = useCallback(async () => {
    setLoadingInf(true); setInfError("");
    try {
      const res = await authed.get("/influencers");
      setInfluencers(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setInfError(err?.response?.data?.message || err.message || "Failed to load");
    } finally { setLoadingInf(false); }
  }, [authed]);

  const loadSentRequests = useCallback(async () => {
    setLoadingReqs(true); setReqsError("");
    try {
      const res = await authed.get("/connections/business");
      const next = Array.isArray(res.data) ? res.data : [];
      setSentReqs(next);

      const currentMap = new Map();
      next.forEach((r) => {
        currentMap.set(r.id, r.status);
        const prevStatus = prevStatusesRef.current.get(r.id);
        if (prevStatus && prevStatus !== r.status) {
          if (r.status === "ACCEPTED") {
            pushNotif(`🎉 @${r.influencerUsername || "influencer"} accepted your request.`);
          } else if (r.status === "REJECTED") {
            pushNotif(`❌ @${r.influencerUsername || "influencer"} rejected your request.`);
          }
        }
      });
      prevStatusesRef.current = currentMap;
    } catch (err) {
      setReqsError(err?.response?.data?.message || err.message || "Failed to load");
    } finally { setLoadingReqs(false); }
  }, [authed, pushNotif]);

  // Load both on mount so nav badges are live immediately
  useEffect(() => { loadInfluencers(); }, [loadInfluencers]);
  useEffect(() => { loadSentRequests(); }, [loadSentRequests]);

  const loadProfile = useCallback(async () => {
    try {
      const res = await authed.get("/businesses/me");
      if (res.data) setProfile({ name: res.data.name || "", industry: res.data.industry || "", location: res.data.location || "" });
    } catch (err) { }
  }, [authed]);
  useEffect(() => { loadProfile(); }, [loadProfile]);

  // Re-fetch sent requests every time the tab is opened (catches status changes)
  useEffect(() => { if (tab === "requests") loadSentRequests(); }, [tab, loadSentRequests]);

  // Background chat listener for notification bell (works even when chat panel is closed)
  useEffect(() => {
    const acceptedConnections = sentReqs.filter((r) => r.status === "ACCEPTED");
    if (!acceptedConnections.length) return;

    let client = null;
    let cancelled = false;

    Promise.all([
      import("@stomp/stompjs"),
      import("sockjs-client"),
    ])
      .then(([{ Client }, SockJSModule]) => {
        if (cancelled) return;
        const SockJS = SockJSModule.default || SockJSModule;

        client = new Client({
          webSocketFactory: () => new SockJS(`${WS_BASE}/ws-chat`),
          reconnectDelay: 5000,
          onConnect: () => {
            acceptedConnections.forEach((conn) => {
              client.subscribe(`/topic/chat/${conn.id}`, (frame) => {
                try {
                  const msg = JSON.parse(frame.body);
                  if (!msg || msg.sender === myUsername) return;

                  const dedupeKey = `${conn.id}|${msg.sender}|${msg.content}|${msg.sentAt || ""}`;
                  if (seenNotifMessagesRef.current.has(dedupeKey)) return;
                  seenNotifMessagesRef.current.add(dedupeKey);
                  if (seenNotifMessagesRef.current.size > 300) {
                    seenNotifMessagesRef.current = new Set(Array.from(seenNotifMessagesRef.current).slice(-150));
                  }

                  // Avoid noisy duplicate if the same chat is currently open
                  if (chatConn?.id === conn.id) return;

                  pushNotif(`💬 New message from @${msg.sender}: ${String(msg.content || "").slice(0, 60)}`);
                } catch {}
              });
            });
          },
        });

        client.activate();
        notifWsClientRef.current = client;
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      try {
        notifWsClientRef.current?.deactivate();
      } catch {}
      notifWsClientRef.current = null;
    };
  }, [sentReqs, myUsername, chatConn, pushNotif]);

  async function saveProfile(e) {
    e.preventDefault();
    setProfileMsg("");
    try {
      await authed.put("/businesses/me", profile);
      setProfileMsg("✅ Profile saved successfully!");
    } catch (err) {
      setProfileMsg("⚠️ Failed to save profile.");
    }
  }

  async function sendRequest(username) {
    setSendingTo(username); setSendMsg("");
    try {
      await authed.post("/connections/request", { influencerUsername: username });
      setSendMsg(`✅ Request sent to @${username}! Check "My Requests" to track its status.`);
      pushNotif(`📤 Request sent to @${username}.`);
    } catch (err) {
      setSendMsg("⚠️ " + (err?.response?.data || err.message));
    } finally { setSendingTo(""); }
  }

  const statusColor = s =>
    s === "ACCEPTED" ? "badge-green" : s === "REJECTED" ? "badge-red" : "badge-yellow";
  const statusIcon = s =>
    s === "ACCEPTED" ? "✅" : s === "REJECTED" ? "❌" : "⏳";

  return (
    <div className="dash">
      <div className="orb orb-1" />
      <aside className="dash-side">
        <div className="dash-logo"><span className="logo-icon">⚡</span> MakeAdv</div>
        <nav className="dash-nav">
          <div className={`dash-nav-item ${tab === "browse" ? "active" : ""}`} onClick={() => setTab("browse")}>
            <span>🔍</span><span className="dash-nav-label">Browse Influencers</span>
          </div>
          <div className={`dash-nav-item ${tab === "requests" ? "active" : ""}`} onClick={() => setTab("requests")}>
            <span>📤</span><span className="dash-nav-label">My Requests</span>
            <span className="dash-nav-badges">
              {sentReqs.filter(r => r.status === "PENDING").length > 0 && (
                <span className="nbadge nbadge-yellow">{sentReqs.filter(r => r.status === "PENDING").length}</span>
              )}
              {sentReqs.filter(r => r.status === "ACCEPTED").length > 0 && (
                <span className="nbadge nbadge-green">{sentReqs.filter(r => r.status === "ACCEPTED").length}</span>
              )}
            </span>
          </div>
          <div className={`dash-nav-item ${tab === "profile" ? "active" : ""}`} onClick={() => setTab("profile")}>
            <span>👤</span><span className="dash-nav-label">My Profile</span>
          </div>
          <div className="dash-nav-item" onClick={onLogout}>🚪 Logout</div>
        </nav>
        <div className="dash-side-foot"><div className="dash-role-pill biz">Business</div></div>
      </aside>

      <main className="dash-main">
        {/* ── BROWSE TAB ── */}
        {tab === "browse" && (
          <>
            <div className="dash-topbar">
              <div>
                <h1 className="dash-heading">Browse Influencers</h1>
                <p className="dash-sub">Find and connect with verified creators for your campaigns.</p>
              </div>
              <div className="dash-topbar-actions">
                <NotificationCenter
                  notifications={notifications}
                  unreadCount={unreadCount}
                  open={notifOpen}
                  onToggle={() => setNotifOpen((v) => !v)}
                  onMarkAllRead={() => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))}
                  onClearAll={() => setNotifications([])}
                />
                <button className="btn-ghost small" onClick={loadInfluencers} disabled={loadingInf}>
                  {loadingInf ? "⟳ Loading…" : "⟳ Refresh"}
                </button>
              </div>
            </div>

            {sendMsg && (
              <div className={`flash ${sendMsg.startsWith("✅") ? "flash-ok" : "flash-err"}`}>
                {sendMsg}
                {sendMsg.startsWith("✅") && (
                  <button className="flash-link" onClick={() => setTab("requests")}>View My Requests →</button>
                )}
              </div>
            )}
            {infError && <div className="flash flash-err">{infError}</div>}

            <div className="inf-grid">
              {loadingInf && <div className="empty-state">Loading influencers…</div>}
              {!loadingInf && influencers.length === 0 && <div className="empty-state">No influencers found. Try refreshing.</div>}
              {influencers.map((inf, i) => (
                <div className="inf-card" key={inf?.username ?? i}>
                  <div className="inf-card-top">
                    <div className="inf-avatar">{(inf?.name || "?")[0].toUpperCase()}</div>
                    <div>
                      <div className="inf-name">{inf?.name || "Unnamed"}</div>
                      <div className="inf-handle">@{inf?.username || "—"}</div>
                    </div>
                    <div className="trust-pill">{inf?.trustScore ?? 0}</div>
                  </div>
                  <div className="inf-tags">
                    {inf?.category && <span className="tag">{inf.category}</span>}
                    {inf?.location && <span className="tag">{inf.location}</span>}
                  </div>
                  <div className="inf-stats">
                    <div className="inf-stat"><div className="snum">{(inf?.followers ?? 0).toLocaleString()}</div><div className="slbl">Followers</div></div>
                    <div className="inf-stat"><div className="snum">{inf?.posts ?? 0}</div><div className="slbl">Posts</div></div>
                    <div className="inf-stat"><div className="snum">{inf?.likes ?? 0}</div><div className="slbl">Likes</div></div>
                  </div>
                  <button className="btn-primary full" disabled={!inf?.username || sendingTo === inf.username} onClick={() => sendRequest(inf.username)}>
                    {sendingTo === inf.username ? "Sending…" : "Send Request"}
                  </button>
                </div>
              ))}
            </div>
          </>
        )}

        {/* ── MY REQUESTS TAB ── */}
        {tab === "requests" && (
          <>
            <div className="dash-topbar">
              <div>
                <h1 className="dash-heading">My Requests</h1>
                <p className="dash-sub">Track the status of collaboration requests you've sent to influencers.</p>
              </div>
              <div className="dash-topbar-actions">
                <NotificationCenter
                  notifications={notifications}
                  unreadCount={unreadCount}
                  open={notifOpen}
                  onToggle={() => setNotifOpen((v) => !v)}
                  onMarkAllRead={() => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))}
                  onClearAll={() => setNotifications([])}
                />
                <button className="btn-ghost small" onClick={loadSentRequests} disabled={loadingReqs}>
                  {loadingReqs ? "⟳ Loading…" : "⟳ Refresh"}
                </button>
              </div>
            </div>

            {reqsError && <div className="flash flash-err">{reqsError}</div>}

            {/* Summary pills */}
            {sentReqs.length > 0 && (
              <div className="req-summary">
                <div className="req-summary-pill yellow">⏳ Pending: {sentReqs.filter(r => r.status === "PENDING").length}</div>
                <div className="req-summary-pill green">✅ Accepted: {sentReqs.filter(r => r.status === "ACCEPTED").length}</div>
                <div className="req-summary-pill red">❌ Rejected: {sentReqs.filter(r => r.status === "REJECTED").length}</div>
              </div>
            )}

            <div className="req-list">
              {loadingReqs && <div className="empty-state">Loading requests…</div>}
              {!loadingReqs && sentReqs.length === 0 && (
                <div className="empty-state">
                  You haven't sent any requests yet.{" "}
                  <button className="link-btn" onClick={() => setTab("browse")}>Browse influencers →</button>
                </div>
              )}
              {sentReqs.map(r => (
                <div className={`req-card req-card-${r.status?.toLowerCase()}`} key={r.id}>
                  <div className="req-left">
                    <div className="req-avatar inf-avatar-sm">{(r.influencerUsername || "I")[0].toUpperCase()}</div>
                    <div>
                      <div className="req-biz">@{r.influencerUsername || "—"}</div>
                      <div className="req-id">Request #{r.id}</div>
                    </div>
                  </div>
                  <div className="req-right">
                    <span className={`badge ${statusColor(r.status)}`}>
                      {statusIcon(r.status)} {r.status}
                    </span>
                    {r.status === "ACCEPTED" && (
                      <>
                        <span className="req-note accepted-note">🎉 Influencer accepted! Collaboration confirmed.</span>
                        <button
                          className="btn-chat"
                          onClick={() => {
                            setChatConn({ id: r.id, otherUsername: r.influencerUsername });
                            pushNotif(`💬 Chat opened with @${r.influencerUsername}.`);
                          }}
                        >
                          💬 Chat
                        </button>
                      </>
                    )}
                    {r.status === "REJECTED" && (
                      <span className="req-note rejected-note">❌ Influencer declined your request.</span>
                    )}
                    {r.status === "PENDING" && (
                      <span className="req-note">⏳ Waiting for influencer response…</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {/* ── PROFILE TAB ── */}
        {tab === "profile" && (
          <>
            <div className="dash-topbar">
              <div>
                <h1 className="dash-heading">My Profile</h1>
                <p className="dash-sub">Update your business details.</p>
              </div>
              <div className="dash-topbar-actions">
                <NotificationCenter
                  notifications={notifications}
                  unreadCount={unreadCount}
                  open={notifOpen}
                  onToggle={() => setNotifOpen((v) => !v)}
                  onMarkAllRead={() => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))}
                  onClearAll={() => setNotifications([])}
                />
              </div>
            </div>
            {profileMsg && <div className={`flash ${profileMsg.startsWith("✅") ? "flash-ok" : "flash-err"}`}>{profileMsg}</div>}

            <div className="profile-container">
              <form onSubmit={saveProfile} className="auth-form profile-form">
                <div className="field">
                  <label>Company Name</label>
                  <input type="text" value={profile.name} onChange={e => setProfile({ ...profile, name: e.target.value })} placeholder="e.g. Acme Corp" />
                </div>
                <div className="field">
                  <label>Industry</label>
                  <input type="text" value={profile.industry} onChange={e => setProfile({ ...profile, industry: e.target.value })} placeholder="e.g. Technology" />
                </div>
                <div className="field">
                  <label>Location</label>
                  <input type="text" value={profile.location} onChange={e => setProfile({ ...profile, location: e.target.value })} placeholder="e.g. New York, NY" />
                </div>
                <button type="submit" className="submit-btn submit-biz">Save Profile</button>
              </form>
            </div>
          </>
        )}
      </main>

      {/* Business Chat overlay */}
      {chatConn && (
        <ChatPanel
          connectionId={chatConn.id}
          myUsername={myUsername}
          otherUsername={chatConn.otherUsername}
          token={token}
          onClose={() => setChatConn(null)}
        />
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   INFLUENCER DASHBOARD  (defined OUTSIDE App)
───────────────────────────────────────────── */
function InfluencerDash({ authed, onLogout, token, myUsername }) {
  const [tab, setTab] = useState("requests");
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // ── Chat state ────────────────────────────────────────────────────
  const [chatConn, setChatConn] = useState(null);
  const [actingOn, setActingOn] = useState(0);
  const [actionMsg, setActionMsg] = useState(""); // confirmation toast

  const [profile, setProfile] = useState({
    name: "", category: "", location: "", followers: 0, following: 0, posts: 0, likes: 0, comments: 0,
    instagramUrl: "", instagramVerified: false
  });
  const [profileMsg, setProfileMsg] = useState("");
  const [notifications, setNotifications] = useState([]);
  const [notifOpen, setNotifOpen] = useState(false);
  const prevReqRef = useRef(new Map());
  const notifWsClientRef = useRef(null);
  const seenNotifMessagesRef = useRef(new Set());

  const unreadCount = useMemo(
    () => notifications.reduce((acc, n) => acc + (n.read ? 0 : 1), 0),
    [notifications]
  );

  const pushNotif = useCallback((message) => {
    setNotifications((prev) => [
      { id: `${Date.now()}-${Math.random()}`, message, createdAt: Date.now(), read: false },
      ...prev.slice(0, 29),
    ]);
  }, []);

  const loadRequests = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const res = await authed.get("/connections/influencer");
      const next = Array.isArray(res.data) ? res.data : [];
      setRequests(next);

      const currentMap = new Map();
      next.forEach((r) => {
        currentMap.set(r.id, r.status);
        const seen = prevReqRef.current.has(r.id);
        if (!seen && r.status === "PENDING") {
          pushNotif(`📬 New collaboration request from ${r.businessName || "a business"}.`);
        }
      });
      prevReqRef.current = currentMap;
    } catch (err) {
      setError(err?.response?.data?.message || err.message || "Failed to load");
    } finally {
      setLoading(false);
    }
  }, [authed, pushNotif]);

  // Load once on mount only
  useEffect(() => { loadRequests(); }, [loadRequests]);

  const loadProfile = useCallback(async () => {
    try {
      const res = await authed.get("/influencers/me");
      if (res.data) setProfile({
        name: res.data.name || "", category: res.data.category || "", location: res.data.location || "",
        followers: res.data.followers || 0, following: res.data.following || 0, posts: res.data.posts || 0,
        likes: res.data.likes || 0, comments: res.data.comments || 0,
        instagramUrl: res.data.instagramUrl || "", instagramVerified: res.data.instagramVerified || false
      });
    } catch (err) { }
  }, [authed]);
  useEffect(() => { loadProfile(); }, [loadProfile]);

  // Background chat listener for notification bell (works even when chat panel is closed)
  useEffect(() => {
    const acceptedConnections = requests.filter((r) => r.status === "ACCEPTED");
    if (!acceptedConnections.length) return;

    let client = null;
    let cancelled = false;

    Promise.all([
      import("@stomp/stompjs"),
      import("sockjs-client"),
    ])
      .then(([{ Client }, SockJSModule]) => {
        if (cancelled) return;
        const SockJS = SockJSModule.default || SockJSModule;

        client = new Client({
          webSocketFactory: () => new SockJS(`${WS_BASE}/ws-chat`),
          reconnectDelay: 5000,
          onConnect: () => {
            acceptedConnections.forEach((conn) => {
              client.subscribe(`/topic/chat/${conn.id}`, (frame) => {
                try {
                  const msg = JSON.parse(frame.body);
                  if (!msg || msg.sender === myUsername) return;

                  const dedupeKey = `${conn.id}|${msg.sender}|${msg.content}|${msg.sentAt || ""}`;
                  if (seenNotifMessagesRef.current.has(dedupeKey)) return;
                  seenNotifMessagesRef.current.add(dedupeKey);
                  if (seenNotifMessagesRef.current.size > 300) {
                    seenNotifMessagesRef.current = new Set(Array.from(seenNotifMessagesRef.current).slice(-150));
                  }

                  // Avoid noisy duplicate if the same chat is currently open
                  if (chatConn?.id === conn.id) return;

                  pushNotif(`💬 New message from ${msg.sender}: ${String(msg.content || "").slice(0, 60)}`);
                } catch {}
              });
            });
          },
        });

        client.activate();
        notifWsClientRef.current = client;
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      try {
        notifWsClientRef.current?.deactivate();
      } catch {}
      notifWsClientRef.current = null;
    };
  }, [requests, myUsername, chatConn, pushNotif]);

  async function saveProfile(e) {
    e.preventDefault();
    setProfileMsg("");
    try {
      const res = await authed.put("/influencers/me", profile);
      if (res.data) {
        setProfile({
          name: res.data.name || "", category: res.data.category || "", location: res.data.location || "",
          followers: res.data.followers || 0, following: res.data.following || 0, posts: res.data.posts || 0,
          likes: res.data.likes || 0, comments: res.data.comments || 0,
          instagramUrl: res.data.instagramUrl || "", instagramVerified: res.data.instagramVerified || false
        });
      }
      setProfileMsg("✅ Profile saved successfully!");
    } catch (err) {
      setProfileMsg("⚠️ Failed to save profile.");
    }
  }

  async function actOn(id, action, businessName) {
    setActingOn(id);
    setActionMsg("");
    try {
      await authed.put(`/connections/${id}/${action}`);
      await loadRequests();
      if (action === "accept") {
        setActionMsg(`✅ You accepted the collaboration request from ${businessName || "the business"}!`);
        pushNotif(`✅ You accepted request from ${businessName || "the business"}.`);
      } else {
        setActionMsg(`❌ You declined the request from ${businessName || "the business"}.`);
        pushNotif(`❌ You rejected request from ${businessName || "the business"}.`);
      }
    } catch (err) {
      setError(err?.response?.data || err.message);
    } finally {
      setActingOn(0);
    }
  }

  const statusColor = s =>
    s === "ACCEPTED" ? "badge-green" : s === "REJECTED" ? "badge-red" : "badge-yellow";
  const statusIcon = s =>
    s === "ACCEPTED" ? "✅" : s === "REJECTED" ? "❌" : "⏳";

  return (
    <div className="dash">
      <div className="orb orb-1" />
      <aside className="dash-side">
        <div className="dash-logo"><span className="logo-icon">⚡</span> MakeAdv</div>
        <nav className="dash-nav">
          <div className={`dash-nav-item ${tab === "requests" ? "active" : ""}`} onClick={() => setTab("requests")}>
            <span>📬</span><span className="dash-nav-label">Brand Requests</span>
            <span className="dash-nav-badges">
              {requests.filter(r => r.status === "PENDING").length > 0 && (
                <span className="nbadge nbadge-yellow">{requests.filter(r => r.status === "PENDING").length}</span>
              )}
            </span>
          </div>
          <div className={`dash-nav-item ${tab === "profile" ? "active" : ""}`} onClick={() => setTab("profile")}>
            <span>👤</span><span className="dash-nav-label">My Profile</span>
          </div>
          <div className="dash-nav-item" onClick={onLogout}>🚪 Logout</div>
        </nav>
        <div className="dash-side-foot"><div className="dash-role-pill inf">Influencer</div></div>
      </aside>

      <main className="dash-main">
        {tab === "requests" && (
          <>
            <div className="dash-topbar">
              <div>
                <h1 className="dash-heading">Brand Requests</h1>
                <p className="dash-sub">Review and respond to collaboration requests from businesses.</p>
              </div>
              <div className="dash-topbar-actions">
                <NotificationCenter
                  notifications={notifications}
                  unreadCount={unreadCount}
                  open={notifOpen}
                  onToggle={() => setNotifOpen((v) => !v)}
                  onMarkAllRead={() => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))}
                  onClearAll={() => setNotifications([])}
                />
                <button className="btn-ghost small" onClick={loadRequests} disabled={loading}>
                  {loading ? "⟳ Loading…" : "⟳ Refresh"}
                </button>
              </div>
            </div>

            {/* Action confirmation toast */}
            {actionMsg && (
              <div className={`flash ${actionMsg.startsWith("✅") ? "flash-ok" : "flash-err"}`}>
                {actionMsg}
              </div>
            )}
            {error && <div className="flash flash-err">{error}</div>}

            {/* Summary pills */}
            {requests.length > 0 && (
              <div className="req-summary">
                <div className="req-summary-pill yellow">⏳ Pending: {requests.filter(r => r.status === "PENDING").length}</div>
                <div className="req-summary-pill green">✅ Accepted: {requests.filter(r => r.status === "ACCEPTED").length}</div>
                <div className="req-summary-pill red">❌ Rejected: {requests.filter(r => r.status === "REJECTED").length}</div>
              </div>
            )}

            <div className="req-list">
              {loading && <div className="empty-state">Loading requests…</div>}
              {!loading && requests.length === 0 && (
                <div className="empty-state">No requests yet. Businesses will reach out soon!</div>
              )}
              {requests.map(r => (
                <div className="req-card" key={r.id}>
                  <div className="req-left">
                    <div className="req-avatar">{(r.businessName || "B")[0].toUpperCase()}</div>
                    <div>
                      <div className="req-biz">{r.businessName || "—"}</div>
                      <div className="req-id">Request #{r.id}</div>
                    </div>
                  </div>
                  <div className="req-right">
                    <span className={`badge ${statusColor(r.status)}`}>
                      {statusIcon(r.status)} {r.status}
                    </span>
                    {r.status === "ACCEPTED" && (
                      <>
                        <span className="req-note accepted-note">🎉 Collaboration confirmed!</span>
                        <button
                          className="btn-chat"
                          onClick={() => {
                            setChatConn({ id: r.id, otherUsername: r.businessName });
                            pushNotif(`💬 Chat opened with ${r.businessName || "business"}.`);
                          }}
                        >
                          💬 Chat
                        </button>
                      </>
                    )}
                    {r.status === "REJECTED" && (
                      <span className="req-note rejected-note">You declined this request.</span>
                    )}
                    {r.status === "PENDING" && (
                      <div className="req-actions">
                        <button
                          className="btn-reject"
                          disabled={actingOn === r.id}
                          onClick={() => actOn(r.id, "reject", r.businessName)}
                        >
                          {actingOn === r.id ? "…" : "Reject"}
                        </button>
                        <button
                          className="btn-accept"
                          disabled={actingOn === r.id}
                          onClick={() => actOn(r.id, "accept", r.businessName)}
                        >
                          {actingOn === r.id ? "…" : "Accept"}
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {/* ── PROFILE TAB ── */}
        {tab === "profile" && (
          <>
            <div className="dash-topbar">
              <div>
                <h1 className="dash-heading">My Profile</h1>
                <p className="dash-sub">Update your metrics to improve your trust score.</p>
              </div>
              <div className="dash-topbar-actions">
                <NotificationCenter
                  notifications={notifications}
                  unreadCount={unreadCount}
                  open={notifOpen}
                  onToggle={() => setNotifOpen((v) => !v)}
                  onMarkAllRead={() => setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))}
                  onClearAll={() => setNotifications([])}
                />
              </div>
            </div>
            {profileMsg && <div className={`flash ${profileMsg.startsWith("✅") ? "flash-ok" : "flash-err"}`}>{profileMsg}</div>}

            <div className="profile-container">
              <form onSubmit={saveProfile} className="auth-form profile-form">
                <div className="profile-grid">
                  <div className="field" style={{ gridColumn: "1 / -1" }}>
                    <label>
                      Instagram Username
                      {profile.instagramVerified && <span style={{ color: "var(--success)", fontSize: "0.85em", marginLeft: "8px" }}>✅ Verified Sync</span>}
                    </label>
                    <input type="text" value={profile.instagramUrl} onChange={e => setProfile({ ...profile, instagramUrl: e.target.value })} placeholder="e.g. virat.kohli" />
                    <small style={{ color: "var(--text-dim)", marginTop: "4px", display: "block" }}>Enter your username to automatically sync your true follower count.</small>
                  </div>
                  <div className="field"><label>Name</label><input type="text" value={profile.name} onChange={e => setProfile({ ...profile, name: e.target.value })} /></div>
                  <div className="field"><label>Category</label><input type="text" value={profile.category} onChange={e => setProfile({ ...profile, category: e.target.value })} /></div>
                  <div className="field"><label>Location</label><input type="text" value={profile.location} onChange={e => setProfile({ ...profile, location: e.target.value })} /></div>
                  <div className="field"><label>Followers</label><input type="number" value={profile.followers} onChange={e => setProfile({ ...profile, followers: parseInt(e.target.value) || 0 })} disabled={profile.instagramVerified} title={profile.instagramVerified ? "Auto-synced from Instagram" : ""} /></div>
                  <div className="field"><label>Following</label><input type="number" value={profile.following} onChange={e => setProfile({ ...profile, following: parseInt(e.target.value) || 0 })} disabled={profile.instagramVerified} title={profile.instagramVerified ? "Auto-synced from Instagram" : ""} /></div>
                  <div className="field"><label>Posts</label><input type="number" value={profile.posts} onChange={e => setProfile({ ...profile, posts: parseInt(e.target.value) || 0 })} disabled={profile.instagramVerified} title={profile.instagramVerified ? "Auto-synced from Instagram" : ""} /></div>
                  <div className="field"><label>Avg Likes</label><input type="number" value={profile.likes} onChange={e => setProfile({ ...profile, likes: parseInt(e.target.value) || 0 })} disabled={profile.instagramVerified} title={profile.instagramVerified ? "Auto-synced from Instagram" : ""} /></div>
                  <div className="field"><label>Avg Comments</label><input type="number" value={profile.comments} onChange={e => setProfile({ ...profile, comments: parseInt(e.target.value) || 0 })} disabled={profile.instagramVerified} title={profile.instagramVerified ? "Auto-synced from Instagram" : ""} /></div>
                </div>
                <button type="submit" className="submit-btn submit-inf">Save Profile</button>
              </form>
            </div>
          </>
        )}
      </main>

      {/* Influencer Chat overlay */}
      {chatConn && (
        <ChatPanel
          connectionId={chatConn.id}
          myUsername={myUsername}
          otherUsername={chatConn.otherUsername}
          token={token}
          onClose={() => setChatConn(null)}
        />
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   ROOT APP  (only manages auth state)
───────────────────────────────────────────── */
function App() {
  const navigate = useNavigate();
  const [token, setToken] = useState(() => localStorage.getItem("makeadv_token") || "");
  const [userRole, setUserRole] = useState(() => localStorage.getItem("makeadv_role") || "");
  const [myUsername, setMyUsername] = useState(() => localStorage.getItem("makeadv_username") || "");

  // Stable axios instance — only recreated when token changes
  const authed = useMemo(() => {
    const instance = axios.create({ baseURL: API_BASE });
    instance.interceptors.request.use(cfg => {
      if (token) cfg.headers.Authorization = `Bearer ${token}`;
      return cfg;
    });
    return instance;
  }, [token]);

  const handleLogin = useCallback((jwt, role, username) => {
    setToken(jwt);
    setUserRole(role);
    setMyUsername(username || "");
    localStorage.setItem("makeadv_token", jwt);
    localStorage.setItem("makeadv_role", role);
    if (username) localStorage.setItem("makeadv_username", username);
  }, []);

  const handleLogout = useCallback(() => {
    setToken("");
    setUserRole("");
    setMyUsername("");
    localStorage.removeItem("makeadv_token");
    localStorage.removeItem("makeadv_role");
    localStorage.removeItem("makeadv_username");
    navigate("/", { replace: true });
  }, [navigate]);

  const dest = userRole === "INFLUENCER" ? "/influencer" : "/business";

  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={token ? <Navigate to={dest} replace /> : <AuthPage mode="login" onLogin={handleLogin} />} />
      <Route path="/register" element={token ? <Navigate to={dest} replace /> : <AuthPage mode="register" onLogin={handleLogin} />} />
      <Route path="/business" element={token ? <BusinessDash authed={authed} onLogout={handleLogout} token={token} myUsername={myUsername} /> : <Navigate to="/login" replace />} />
      <Route path="/influencer" element={token ? <InfluencerDash authed={authed} onLogout={handleLogout} token={token} myUsername={myUsername} /> : <Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
