import * as React from "react";
import { cn } from "@/lib/utils";

function Input({ className, type, onInvalid, onInput, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "h-9 w-full min-w-0 rounded-md border border-input bg-transparent px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none selection:bg-primary selection:text-primary-foreground file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm dark:bg-input/30",
        "focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50",
        "aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40",
        className,
      )}
      {...props}
      onInvalid={(event) => {
        const input = event.currentTarget;
        input.setCustomValidity("");
        if (input.validity.valueMissing) input.setCustomValidity("Preencha este campo.");
        else if (input.validity.typeMismatch) input.setCustomValidity("Informe um e-mail válido.");
        else if (input.validity.tooShort) input.setCustomValidity(`Use pelo menos ${input.minLength} caracteres.`);
        else if (!input.validity.valid) input.setCustomValidity("Confira o valor informado.");
        onInvalid?.(event);
      }}
      onInput={(event) => { event.currentTarget.setCustomValidity(""); onInput?.(event); }}
    />
  );
}

export { Input };
