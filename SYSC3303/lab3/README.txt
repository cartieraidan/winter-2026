Source Files:
    1. PelicanCrossing.java
    2. PelicanCrossingPublicTest

Run Instructions:
    1. Run test file.

Explanation of Implementation
    In the operational state machine it contain two individual substate machines which I decided to split into two
    private inner classes in PelicanCrossing which handle all of their inner states and actions. While all events
    are still handle by the PelicanCrossing class in the dispatch method implementing the behaviour by calling
    methods from either inner classes or implementing its behaviour within the switch case.