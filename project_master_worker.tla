--------------------------- MODULE project_master_worker ---------------------------

EXTENDS TLC, Integers, Sequences
CONSTANTS NUMBER_OF_WORKERS, TOTAL_PDF

(*--algorithm pcd_assignment
variable pdfMonitor = <<>>,
stateMonitor = "on",

pdfMonitorFree = 1,
stateMonitorFree = 1,

current_pdf_number = 0,
last_pdf = 0;

define
PdfTermination == <>(current_pdf_number = TOTAL_PDF)
PdfMonitorBusy == <>(pdfMonitorFree = 0)
StateMonitorBusy == <>(stateMonitorFree = 0)
WorkerEnd == <>(stateMonitor /= "on")
end define;

macro wait(mutex) begin
  await mutex = 1;  
  mutex := 0;
end macro;

macro signal(mutex) begin
  mutex := 1;
end macro;


fair+ process master = 0
begin

evaluateWhile:
  while current_pdf_number < TOTAL_PDF do

    getPdf:
        current_pdf_number := current_pdf_number+1;
    
    setCurrentDocument:
        wait(pdfMonitorFree);
        
    updateMonitor:
        pdfMonitor := Append(pdfMonitor, current_pdf_number);

    updateLastPdf:
        if current_pdf_number = TOTAL_PDF
        then last_pdf := 1;
        end if;
    
    freePdfMonitorMaster:    
        signal(pdfMonitorFree);
        
  end while; 
end process;

fair+ process worker \in 1..NUMBER_OF_WORKERS
variable pdf = 0,
state = "";
begin

evaluateWhile:
    wait(stateMonitorFree);
    state := stateMonitor;
    
whileEvaluated:
    signal(stateMonitorFree);
    
startWhile:    
    while state = "on" do
    
        getCurrentDocument:
            wait(pdfMonitorFree);
    
        waitForFullMonitor:
        while pdfMonitor = <<>> /\ current_pdf_number < TOTAL_PDF do
            
            wakeOthers:
            signal(pdfMonitorFree);
            
            retryWaitFull:
            wait(pdfMonitorFree);
        end while;
        
        dequeueMonitor:
            if pdfMonitor /= <<>> 
            then pdf := Head(pdfMonitor);
                pdfMonitor := Tail(pdfMonitor);
            
                checkLastPdf:
                if pdfMonitor = <<>> /\ last_pdf = 1 
                then wait(stateMonitorFree);
                    stateMonitor := "off";
                    
                    freeStateMonitor:
                        signal(stateMonitorFree);
                        
                end if;
            end if;
            
        freePdfMonitorWorker:
                    signal(pdfMonitorFree);                
        
        processPdf:
            print pdf;
    
        reEvaluateWhile:
            wait(stateMonitorFree);
            state := stateMonitor;
            
            whileReEvaluated:
                signal(stateMonitorFree);
            
    end while;
end process;
end algorithm;*)
\* BEGIN TRANSLATION (chksum(pcal) = "aaa125dd" /\ chksum(tla) = "7ed9ae31")
\* Label evaluateWhile of process master at line 37 col 3 changed to evaluateWhile_
VARIABLES pdfMonitor, stateMonitor, pdfMonitorFree, stateMonitorFree, 
          current_pdf_number, last_pdf, pc

(* define statement *)
PdfTermination == <>(current_pdf_number = TOTAL_PDF)
PdfMonitorBusy == <>(pdfMonitorFree = 0)
StateMonitorBusy == <>(stateMonitorFree = 0)
WorkerEnd == <>(stateMonitor /= "on")

VARIABLES pdf, state

vars == << pdfMonitor, stateMonitor, pdfMonitorFree, stateMonitorFree, 
           current_pdf_number, last_pdf, pc, pdf, state >>

ProcSet == {0} \cup (1..NUMBER_OF_WORKERS)

Init == (* Global variables *)
        /\ pdfMonitor = <<>>
        /\ stateMonitor = "on"
        /\ pdfMonitorFree = 1
        /\ stateMonitorFree = 1
        /\ current_pdf_number = 0
        /\ last_pdf = 0
        (* Process worker *)
        /\ pdf = [self \in 1..NUMBER_OF_WORKERS |-> 0]
        /\ state = [self \in 1..NUMBER_OF_WORKERS |-> ""]
        /\ pc = [self \in ProcSet |-> CASE self = 0 -> "evaluateWhile_"
                                        [] self \in 1..NUMBER_OF_WORKERS -> "evaluateWhile"]

evaluateWhile_ == /\ pc[0] = "evaluateWhile_"
                  /\ IF current_pdf_number < TOTAL_PDF
                        THEN /\ pc' = [pc EXCEPT ![0] = "getPdf"]
                        ELSE /\ pc' = [pc EXCEPT ![0] = "Done"]
                  /\ UNCHANGED << pdfMonitor, stateMonitor, pdfMonitorFree, 
                                  stateMonitorFree, current_pdf_number, 
                                  last_pdf, pdf, state >>

getPdf == /\ pc[0] = "getPdf"
          /\ current_pdf_number' = current_pdf_number+1
          /\ pc' = [pc EXCEPT ![0] = "setCurrentDocument"]
          /\ UNCHANGED << pdfMonitor, stateMonitor, pdfMonitorFree, 
                          stateMonitorFree, last_pdf, pdf, state >>

setCurrentDocument == /\ pc[0] = "setCurrentDocument"
                      /\ pdfMonitorFree = 1
                      /\ pdfMonitorFree' = 0
                      /\ pc' = [pc EXCEPT ![0] = "updateMonitor"]
                      /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                      stateMonitorFree, current_pdf_number, 
                                      last_pdf, pdf, state >>

updateMonitor == /\ pc[0] = "updateMonitor"
                 /\ pdfMonitor' = Append(pdfMonitor, current_pdf_number)
                 /\ pc' = [pc EXCEPT ![0] = "updateLastPdf"]
                 /\ UNCHANGED << stateMonitor, pdfMonitorFree, 
                                 stateMonitorFree, current_pdf_number, 
                                 last_pdf, pdf, state >>

updateLastPdf == /\ pc[0] = "updateLastPdf"
                 /\ IF current_pdf_number = TOTAL_PDF
                       THEN /\ last_pdf' = 1
                       ELSE /\ TRUE
                            /\ UNCHANGED last_pdf
                 /\ pc' = [pc EXCEPT ![0] = "freePdfMonitorMaster"]
                 /\ UNCHANGED << pdfMonitor, stateMonitor, pdfMonitorFree, 
                                 stateMonitorFree, current_pdf_number, pdf, 
                                 state >>

freePdfMonitorMaster == /\ pc[0] = "freePdfMonitorMaster"
                        /\ pdfMonitorFree' = 1
                        /\ pc' = [pc EXCEPT ![0] = "evaluateWhile_"]
                        /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                        stateMonitorFree, current_pdf_number, 
                                        last_pdf, pdf, state >>

master == evaluateWhile_ \/ getPdf \/ setCurrentDocument \/ updateMonitor
             \/ updateLastPdf \/ freePdfMonitorMaster

evaluateWhile(self) == /\ pc[self] = "evaluateWhile"
                       /\ stateMonitorFree = 1
                       /\ stateMonitorFree' = 0
                       /\ state' = [state EXCEPT ![self] = stateMonitor]
                       /\ pc' = [pc EXCEPT ![self] = "whileEvaluated"]
                       /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                       pdfMonitorFree, current_pdf_number, 
                                       last_pdf, pdf >>

whileEvaluated(self) == /\ pc[self] = "whileEvaluated"
                        /\ stateMonitorFree' = 1
                        /\ pc' = [pc EXCEPT ![self] = "startWhile"]
                        /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                        pdfMonitorFree, current_pdf_number, 
                                        last_pdf, pdf, state >>

startWhile(self) == /\ pc[self] = "startWhile"
                    /\ IF state[self] = "on"
                          THEN /\ pc' = [pc EXCEPT ![self] = "getCurrentDocument"]
                          ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                    /\ UNCHANGED << pdfMonitor, stateMonitor, pdfMonitorFree, 
                                    stateMonitorFree, current_pdf_number, 
                                    last_pdf, pdf, state >>

getCurrentDocument(self) == /\ pc[self] = "getCurrentDocument"
                            /\ pdfMonitorFree = 1
                            /\ pdfMonitorFree' = 0
                            /\ pc' = [pc EXCEPT ![self] = "waitForFullMonitor"]
                            /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                            stateMonitorFree, 
                                            current_pdf_number, last_pdf, pdf, 
                                            state >>

waitForFullMonitor(self) == /\ pc[self] = "waitForFullMonitor"
                            /\ IF pdfMonitor = <<>> /\ current_pdf_number < TOTAL_PDF
                                  THEN /\ pc' = [pc EXCEPT ![self] = "wakeOthers"]
                                  ELSE /\ pc' = [pc EXCEPT ![self] = "dequeueMonitor"]
                            /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                            pdfMonitorFree, stateMonitorFree, 
                                            current_pdf_number, last_pdf, pdf, 
                                            state >>

wakeOthers(self) == /\ pc[self] = "wakeOthers"
                    /\ pdfMonitorFree' = 1
                    /\ pc' = [pc EXCEPT ![self] = "retryWaitFull"]
                    /\ UNCHANGED << pdfMonitor, stateMonitor, stateMonitorFree, 
                                    current_pdf_number, last_pdf, pdf, state >>

retryWaitFull(self) == /\ pc[self] = "retryWaitFull"
                       /\ pdfMonitorFree = 1
                       /\ pdfMonitorFree' = 0
                       /\ pc' = [pc EXCEPT ![self] = "waitForFullMonitor"]
                       /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                       stateMonitorFree, current_pdf_number, 
                                       last_pdf, pdf, state >>

dequeueMonitor(self) == /\ pc[self] = "dequeueMonitor"
                        /\ IF pdfMonitor /= <<>>
                              THEN /\ pdf' = [pdf EXCEPT ![self] = Head(pdfMonitor)]
                                   /\ pdfMonitor' = Tail(pdfMonitor)
                                   /\ pc' = [pc EXCEPT ![self] = "checkLastPdf"]
                              ELSE /\ pc' = [pc EXCEPT ![self] = "freePdfMonitorWorker"]
                                   /\ UNCHANGED << pdfMonitor, pdf >>
                        /\ UNCHANGED << stateMonitor, pdfMonitorFree, 
                                        stateMonitorFree, current_pdf_number, 
                                        last_pdf, state >>

checkLastPdf(self) == /\ pc[self] = "checkLastPdf"
                      /\ IF pdfMonitor = <<>> /\ last_pdf = 1
                            THEN /\ stateMonitorFree = 1
                                 /\ stateMonitorFree' = 0
                                 /\ stateMonitor' = "off"
                                 /\ pc' = [pc EXCEPT ![self] = "freeStateMonitor"]
                            ELSE /\ pc' = [pc EXCEPT ![self] = "freePdfMonitorWorker"]
                                 /\ UNCHANGED << stateMonitor, 
                                                 stateMonitorFree >>
                      /\ UNCHANGED << pdfMonitor, pdfMonitorFree, 
                                      current_pdf_number, last_pdf, pdf, state >>

freeStateMonitor(self) == /\ pc[self] = "freeStateMonitor"
                          /\ stateMonitorFree' = 1
                          /\ pc' = [pc EXCEPT ![self] = "freePdfMonitorWorker"]
                          /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                          pdfMonitorFree, current_pdf_number, 
                                          last_pdf, pdf, state >>

freePdfMonitorWorker(self) == /\ pc[self] = "freePdfMonitorWorker"
                              /\ pdfMonitorFree' = 1
                              /\ pc' = [pc EXCEPT ![self] = "processPdf"]
                              /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                              stateMonitorFree, 
                                              current_pdf_number, last_pdf, 
                                              pdf, state >>

processPdf(self) == /\ pc[self] = "processPdf"
                    /\ PrintT(pdf[self])
                    /\ pc' = [pc EXCEPT ![self] = "reEvaluateWhile"]
                    /\ UNCHANGED << pdfMonitor, stateMonitor, pdfMonitorFree, 
                                    stateMonitorFree, current_pdf_number, 
                                    last_pdf, pdf, state >>

reEvaluateWhile(self) == /\ pc[self] = "reEvaluateWhile"
                         /\ stateMonitorFree = 1
                         /\ stateMonitorFree' = 0
                         /\ state' = [state EXCEPT ![self] = stateMonitor]
                         /\ pc' = [pc EXCEPT ![self] = "whileReEvaluated"]
                         /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                         pdfMonitorFree, current_pdf_number, 
                                         last_pdf, pdf >>

whileReEvaluated(self) == /\ pc[self] = "whileReEvaluated"
                          /\ stateMonitorFree' = 1
                          /\ pc' = [pc EXCEPT ![self] = "startWhile"]
                          /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                          pdfMonitorFree, current_pdf_number, 
                                          last_pdf, pdf, state >>

worker(self) == evaluateWhile(self) \/ whileEvaluated(self)
                   \/ startWhile(self) \/ getCurrentDocument(self)
                   \/ waitForFullMonitor(self) \/ wakeOthers(self)
                   \/ retryWaitFull(self) \/ dequeueMonitor(self)
                   \/ checkLastPdf(self) \/ freeStateMonitor(self)
                   \/ freePdfMonitorWorker(self) \/ processPdf(self)
                   \/ reEvaluateWhile(self) \/ whileReEvaluated(self)

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == master
           \/ (\E self \in 1..NUMBER_OF_WORKERS: worker(self))
           \/ Terminating

Spec == /\ Init /\ [][Next]_vars
        /\ SF_vars(master)
        /\ \A self \in 1..NUMBER_OF_WORKERS : SF_vars(worker(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION 
====
