package sn.auchan.portail.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ApplicationReorderRequest(@NotEmpty List<Long> ids) {
}
