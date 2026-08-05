import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/lib/i18n";

export function Pagination({ page, pageCount, onPageChange }: {
  page: number;
  pageCount: number;
  onPageChange: (page: number) => void;
}) {
  const { t } = useI18n();
  if (pageCount <= 1) return null;
  return (
    <nav className="mt-8 flex items-center justify-center gap-3" aria-label={`${t("common.page")} ${page} ${t("common.of")} ${pageCount}`}>
      <Button type="button" variant="outline" size="sm" onClick={() => onPageChange(page - 1)} disabled={page <= 1}>
        <ChevronLeft className="mr-1 h-4 w-4" aria-hidden="true" /> {t("common.previous")}
      </Button>
      <span className="min-w-24 text-center text-sm text-muted-foreground">
        {t("common.page")} {page} {t("common.of")} {pageCount}
      </span>
      <Button type="button" variant="outline" size="sm" onClick={() => onPageChange(page + 1)} disabled={page >= pageCount}>
        {t("common.next")} <ChevronRight className="ml-1 h-4 w-4" aria-hidden="true" />
      </Button>
    </nav>
  );
}
