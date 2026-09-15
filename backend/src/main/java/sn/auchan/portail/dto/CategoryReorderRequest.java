package sn.auchan.portail.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CategoryReorderRequest(@NotEmpty List<Long> ids) {
}
