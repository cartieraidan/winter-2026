#lang racket
(require 2htdp/image)
(define a-red-square (rectangle 100 100 "solid" "red")) ; part 1
(define a-blue-circle (circle 50 "solid" "blue")) ; part 2
(define outlined-square (rectangle 100 100 "outline" "red")) ; part 3
(define outlined-circle (circle 50 "outline" "blue")) ; part 3
(define image-1 (beside (rectangle 100 100 "solid" "red")
                        (rectangle 100 100 "solid" "blue")
                        (rectangle 100 100 "solid" "green"))) ; part 4
(define image-2 (above (rectangle 100 100 "solid" "red")
                        (rectangle 100 100 "solid" "blue")
                        (rectangle 100 100 "solid" "green"))) ; part 4
(define image-3 (overlay/align "middle" "middle"
                               (rectangle 20 20 "solid" "black")
                               (rectangle 40 40 "solid" "green")
                               (rectangle 60 60 "solid" "blue")
                               (rectangle 80 80 "solid" "red"))) ; part 4
