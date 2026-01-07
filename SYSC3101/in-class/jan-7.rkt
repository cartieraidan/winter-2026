#lang racket
(define (func) (lambda (x) (* x x))) ; func returns a procedure to be called ((func) x)

(define (bigger x)
  (cond
    [(> x 1) 2 4] ; don't know what this means
    )
  )
(bigger 3)

(define (power x n)
  (define (power-iter n count)
    (if (= n 0)
        count
        (power-iter (- n 1) (* count x)
)))
  (power-iter n 1))
(power 2 3)
