import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";

type Props = React.ComponentProps<typeof Button> & { loading?: boolean; loadingLabel?: string };

export function LoadingButton({ loading = false, loadingLabel, children, disabled, ...props }: Props) {
  return (
    <Button {...props} disabled={disabled || loading} aria-busy={loading || undefined}>
      {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />}
      {loading ? loadingLabel ?? children : children}
    </Button>
  );
}
