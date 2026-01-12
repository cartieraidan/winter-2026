#lang racket
(cons 4 (cons 5 empty))

(cons (sqr 2) empty)

(cons (cons 3 (cons true empty)) (cons 5 empty)) ; two list

(first (cons (cons 3 (cons true empty)) (cons 5 empty))) ; gets first list of two

(car (cons 4 (cons 5 empty))) ; first element

(cdr (cons 4 (cons 5 empty))) ; rest of list, excludes 1

(define lst (cons 4 (cons 5 (cons 7 empty))))

(car (cdr lst)) ; get to second element in list

(rest (rest (rest lst))) ; gets the pointer of last element which is empty

(define mylst (list (list 1 2) 3 4)) ; creates list

(cons 1 '(2 3)) ; appends 2 3 on list
