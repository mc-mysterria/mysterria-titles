package net.mysterria.titles.domain.buff.model.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BonusKind {
    BOOST("+") {
        @Override
        public int percent(double value) {
            return (int) Math.round(value * 100);
        }
    },
    REDUCTION("-") {
        @Override
        public int percent(double value) {
            return (int) Math.round((1 - 1 / (1 + value)) * 100);
        }
    };

    private final String sign;

    public abstract int percent(double value);
}
