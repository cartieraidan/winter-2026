Files Explanation:
    Agent
        Is the producer that adds two random components to the shared memory space for the consumer to consume.

    Technician
        Is the consumer that consumes components from the shared memory and then signals the Agent that it assembled
        a drone.

    Components
        Is the enum class that helps to identify the components.

    AssemblyTable
        Is the shared memory space which holds the methods put and get for the producer and consumer to use.

    ProductionController
        Is the class that controls all thread creating and interruption

Setup Instructions:
    Run main method in ProductionController and an output of thread states will be processed in console.