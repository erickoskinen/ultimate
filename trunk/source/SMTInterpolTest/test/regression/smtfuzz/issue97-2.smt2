(set-option :produce-models true)
(set-option :model-check-mode true)
(set-logic QF_NRA)
(declare-const v0 Bool)
(declare-const v1 Bool)
(declare-const r1 Real)
(declare-const r3 Real)
(declare-const r7 Real)
(declare-const r8 Real)
(declare-const r10 Real)
(declare-const r12 Real)
(declare-const v2 Bool)
(declare-const v3 Bool)
(declare-const r13 Real)
(declare-const v4 Bool)
(declare-const v5 Bool)
(declare-const v6 Bool)
(declare-const v7 Bool)
(declare-const v8 Bool)
(declare-const v9 Bool)
(declare-const r14 Real)
(declare-const v10 Bool)
(declare-const v11 Bool)
(declare-const v12 Bool)
(declare-const v13 Bool)
(declare-const v14 Bool)
(declare-const v15 Bool)
(declare-const v16 Bool)
(declare-const v17 Bool)
(declare-const r15 Real)
(declare-const r16 Real)
(declare-const v18 Bool)
(declare-const v19 Bool)
(declare-const r17 Real)
(declare-const r18 Real)
(declare-const v20 Bool)
(declare-const r19 Real)
(declare-const v21 Bool)
(declare-const v22 Bool)
(declare-const r20 Real)
(declare-const v23 Bool)
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) v23) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) false))
(assert (or (or v23 (<= r1 6.0 (abs r12)) v23) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) v23) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) false))
(assert (or (or (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) false))
(assert (or (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) v23) false))
(assert (or (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) false))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) v23) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) false))
(assert (or (or v23 v23 (<= r1 6.0 (abs r12))) false))
(assert (or (or (<= r1 6.0 (abs r12)) v23 v23) false))
(assert (or (or (<= r1 6.0 (abs r12)) v23 v23) false))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) false))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12))) false))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) false))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) v23) false))
(assert (or (or v23 (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) false))
(assert (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))))
(assert (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)))
(assert (and (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))) (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) (or (<= r1 6.0 (abs r12)) v23 v23) (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0))))
(assert (=> (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) v23)))
(assert (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (=> (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) v23))))
(assert (=> (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0))))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 6.0 (abs r12)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))))
(assert (or (or (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))) (or (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))) (or (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or v23 v23 (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0)) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))) (or (<= r1 6.0 (abs r12)) v23 v23) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 6.0 (abs r12)) (<= r1 6.0 (abs r12))) (or (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (<= r1 (+ (/ r7 0.0) r10 0.53963500 7.0) (- 0.537660775 (+ 489508.0 r1 489508.0) (- 0.537660775 57375.0 0.537660775 999695.0) r12) 0.537660775 71012447.0) (>= r10 (/ r7 0.0) 0.53963500 (/ r3 989261.0)))))
(check-sat)
(exit)
