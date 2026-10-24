"use client";

import { FormEvent, useState } from "react";

const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

type AuthResponse = {
  accessToken: string;
  userId: string;
  fullName: string;
  email: string;
  role: "ADMIN" | "CLIENT" | string;
  message?: string;
  detail?: string;
  title?: string;
};

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");

    try {
      const response = await fetch(`${apiUrl}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim(), password }),
      });
      const data = (await response.json().catch(() => ({}))) as Partial<AuthResponse>;

      if (!response.ok) {
        throw new Error(data.message || data.detail || data.title || "Login failed. Check your credentials and try again.");
      }
      if (!data.accessToken || !data.role) {
        throw new Error("The server response is missing the login token or account role.");
      }

      sessionStorage.setItem("vk_access_token", data.accessToken);
      sessionStorage.setItem(
        "vk_user",
        JSON.stringify({
          userId: data.userId,
          fullName: data.fullName,
          email: data.email,
          role: data.role,
        }),
      );

      window.location.assign(data.role === "ADMIN" ? "/admin" : "/portal");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Could not connect to the server.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-5">
      <form
        onSubmit={submit}
        className="w-full max-w-md rounded-3xl border border-white/15 bg-white/[.03] p-8"
      >
        <a href="/" className="text-xs tracking-[.3em] text-amber-200">
          VK CREATION
        </a>
        <h1 className="mt-7 text-3xl font-semibold">Welcome back</h1>
        <p className="mt-2 text-sm text-white/55">Sign in to view your bookings.</p>

        <label className="mt-8 block text-sm">
          Email
          <input
            autoComplete="email"
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3 outline-none focus:border-amber-200"
            type="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </label>

        <label className="mt-4 block text-sm">
          Password
          <input
            autoComplete="current-password"
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3 outline-none focus:border-amber-200"
            type="password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        <button
          disabled={busy}
          className="mt-6 w-full rounded-full bg-amber-200 p-3 font-medium text-black disabled:opacity-50"
        >
          {busy ? "Signing in…" : "Sign in"}
        </button>

        {message && (
          <p role="alert" className="mt-4 text-sm text-amber-100">
            {message}
          </p>
        )}

        <p className="mt-5 text-sm text-white/55">
          New here?{" "}
          <a className="text-amber-200" href="/register">
            Create an account
          </a>
        </p>
        <p className="mt-5 text-xs text-white/40">API: {apiUrl}</p>
      </form>
    </main>
  );
}
