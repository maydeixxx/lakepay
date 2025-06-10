import { USERS_URL } from "@/config";
import type { User } from "@/types";
import { useCallback, useEffect, useRef, useState } from "react";

export function useUser(userId: number | string) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchUser = useCallback(async () => {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch(`${USERS_URL}/${userId}`, {
        signal: abortControllerRef.current?.signal
      });

      const user = (await response.json()) as User;
      setUser(user);
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }

      setError(e);
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchUser();
  }, [userId]);

  return {
    user,
    error,
    isLoading,
    invalidate: fetchUser
  };
}
