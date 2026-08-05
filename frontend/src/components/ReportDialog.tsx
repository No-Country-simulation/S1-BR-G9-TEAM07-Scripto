import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { reportReasonLabel } from "@/lib/display";
import { useI18n } from "@/lib/i18n";
import type { ReportReason } from "@/services/report.service";

const reasons: ReportReason[] = ["SPAM", "HARASSMENT", "HATEFUL_CONTENT", "ILLEGAL_CONTENT", "COPYRIGHT", "MISINFORMATION", "OTHER"];

export function ReportDialog({
  open,
  documentTitle,
  loading = false,
  onOpenChange,
  onSubmit,
}: {
  open: boolean;
  documentTitle?: string;
  loading?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (reason: ReportReason, details?: string) => void | Promise<void>;
}) {
  const { t } = useI18n();
  const [reason, setReason] = useState<ReportReason>("SPAM");
  const [details, setDetails] = useState("");

  async function submit() {
    await onSubmit(reason, details.trim() || undefined);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("explore.report.title")}</DialogTitle>
          <DialogDescription>{documentTitle}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="shared-report-reason">{t("explore.report.reason")}</Label>
            <select id="shared-report-reason" value={reason} onChange={(event) => setReason(event.target.value as ReportReason)} className="min-h-10 w-full rounded-md border border-input bg-background px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
              {reasons.map((value) => <option key={value} value={value}>{reportReasonLabel(value, t)}</option>)}
            </select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="shared-report-details">{t("explore.report.details")} ({t("common.optional")})</Label>
            <Textarea id="shared-report-details" rows={5} maxLength={500} value={details} onChange={(event) => setDetails(event.target.value)} />
            <p className="text-right text-xs text-muted-foreground">{details.length}/500</p>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>{t("common.cancel")}</Button>
          <Button onClick={() => void submit()} disabled={loading} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{loading ? t("common.loading") : t("explore.report.submit")}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
