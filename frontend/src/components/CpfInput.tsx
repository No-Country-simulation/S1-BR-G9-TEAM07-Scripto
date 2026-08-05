import { Input } from "@/components/ui/input";
import { maskCpf } from "@/lib/validation";

type Props = Omit<React.ComponentProps<typeof Input>, "value" | "onChange"> & {
  value: string;
  onValueChange: (value: string) => void;
};

export function CpfInput({ value, onValueChange, ...props }: Props) {
  return (
    <Input
      {...props}
      value={value}
      inputMode="numeric"
      autoComplete="off"
      placeholder="000.000.000-00"
      onChange={(event) => onValueChange(maskCpf(event.target.value))}
    />
  );
}
