import type { ComponentProps } from "react";
import { Input } from "@/components/ui/input";

export function EmailInput(props: ComponentProps<typeof Input>) {
  return <Input type="email" inputMode="email" autoComplete="email" spellCheck={false} {...props} />;
}
