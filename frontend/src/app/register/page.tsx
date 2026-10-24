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

export default function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");

    try {
      const response = await fetch(`${apiUrl}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          fullName: fullName.trim(),
          email: email.trim(),
          phone: phone.trim(),
          password,
        }),
      });
      const data = (await response.json().catch(() => ({}))) as Partial<AuthResponse>;

      if (!response.ok) {
        throw new Error(data.message || data.detail || data.title || "Registration failed. Please check your details.");
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
        <h1 className="mt-7 text-3xl font-semibold">Create your account</h1>

        <label className="mt-6 block text-sm">
          Full name
          <input
            required
            maxLength={160}
            autoComplete="name"
            value={fullName}
            onChange={(event) => setFullName(event.target.value)}
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3"
          />
        </label>
        <label className="mt-4 block text-sm">
          Email
          <input
            required
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3"
          />
        </label>
        <label className="mt-4 block text-sm">
          Phone (optional)
          <input
            maxLength={32}
            autoComplete="tel"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3"
          />
        </label>
        <label className="mt-4 block text-sm">
          Password (minimum 8 characters)
          <input
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-2 w-full rounded-xl border border-white/15 bg-black p-3"
          />
        </label>

        <button
          disabled={busy}
          className="mt-6 w-full rounded-full bg-amber-200 p-3 font-medium text-black disabled:opacity-50"
        >
          {busy ? "Creating…" : "Create account"}
        </button>
        {message && (
          <p role="alert" className="mt-4 text-sm text-amber-100">
            {message}
          </p>
        )}
        <p className="mt-5 text-sm text-white/55">
          Already registered?{" "}
          <a className="text-amber-200" href="/login">
            Sign in
          </a>
        </p>
      </form>
    </main>
  );
}
