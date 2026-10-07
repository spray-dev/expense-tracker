import { useEffect, useState } from "react";
import { requestError } from "./finance";

export function useResource<T>(key: string, load: (signal: AbortSignal) => Promise<T>) {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState<{ key: string; attempt: number; data?: T; error?: string }>();
  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal).then(data => {
      if (!controller.signal.aborted) setResult({ key, attempt, data });
    }).catch(error => {
      if (!controller.signal.aborted) setResult({ key, attempt, error: requestError(error) });
    });
    return () => controller.abort();
  }, [key, attempt, load]);
  const current = result?.key === key && result.attempt === attempt ? result : undefined;
  return { data: current?.data, error: current?.error, loading: !current, refresh: () => setAttempt(value => value + 1) };
}
