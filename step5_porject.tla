--------------------------- MODULE step5_porject ---------------------------

EXTENDS TLC, Integers, Sequences
CONSTANTS NUMBER_OF_WORKERS, TOTAL_PDF

(*--algorithm pcd_assignment
variable pdfMonitor = <<>>,
stateMonitor = "on",
pdf_number = 0;
define
NoOverflowInvariant == Len(pdfMonitor) <= NUMBER_OF_WORKERS
end define;

fair process master = "master"
variable pdf = 0,
tmp = 0;
begin Produce:
  while pdf_number < TOTAL_PDF do
    getPdf:
        pdf_number := pdf_number+1;
        pdf := pdf_number;
    putInMonitor: 
        await pdfMonitor = <<>>;
        updateMonitor:
            while(tmp < NUMBER_OF_WORKERS) do
                pdfMonitor := Append(pdfMonitor, pdf);
                tmp := tmp + 1
            end while;
        tmp := 0
  end while;
end process;

fair process worker \in {"worker1", "worker2"}
variable pdf = 999;
begin Consume:
  while stateMonitor = "on" do
    getFromMonitor: 
        await pdfMonitor /= <<>> \/ pdf_number = TOTAL_PDF;
        if pdfMonitor /= <<>> 
        then pdf := Head(pdfMonitor);
            pdfMonitor := Tail(pdfMonitor);
            if pdfMonitor = <<>> /\ pdf_number = TOTAL_PDF 
            then stateMonitor := "off";
                 
            end if;
            processPdf:
                print pdf;
        end if;
  end while;
end process;
end algorithm;*)
\* BEGIN TRANSLATION (chksum(pcal) = "cfda89f8" /\ chksum(tla) = "b63badd6")
\* Process variable pdf of process master at line 15 col 10 changed to pdf_
VARIABLES pdfMonitor, stateMonitor, pdf_number, pc

(* define statement *)
NoOverflowInvariant == Len(pdfMonitor) <= NUMBER_OF_WORKERS

VARIABLES pdf_, tmp, pdf

vars == << pdfMonitor, stateMonitor, pdf_number, pc, pdf_, tmp, pdf >>

ProcSet == {"master"} \cup ({"worker1", "worker2"})

Init == (* Global variables *)
        /\ pdfMonitor = <<>>
        /\ stateMonitor = "on"
        /\ pdf_number = 0
        (* Process master *)
        /\ pdf_ = 0
        /\ tmp = 0
        (* Process worker *)
        /\ pdf = [self \in {"worker1", "worker2"} |-> 999]
        /\ pc = [self \in ProcSet |-> CASE self = "master" -> "Produce"
                                        [] self \in {"worker1", "worker2"} -> "Consume"]

Produce == /\ pc["master"] = "Produce"
           /\ IF pdf_number < TOTAL_PDF
                 THEN /\ pc' = [pc EXCEPT !["master"] = "getPdf"]
                 ELSE /\ pc' = [pc EXCEPT !["master"] = "Done"]
           /\ UNCHANGED << pdfMonitor, stateMonitor, pdf_number, pdf_, tmp, 
                           pdf >>

getPdf == /\ pc["master"] = "getPdf"
          /\ pdf_number' = pdf_number+1
          /\ pdf_' = pdf_number'
          /\ pc' = [pc EXCEPT !["master"] = "putInMonitor"]
          /\ UNCHANGED << pdfMonitor, stateMonitor, tmp, pdf >>

putInMonitor == /\ pc["master"] = "putInMonitor"
                /\ pdfMonitor = <<>>
                /\ pc' = [pc EXCEPT !["master"] = "updateMonitor"]
                /\ UNCHANGED << pdfMonitor, stateMonitor, pdf_number, pdf_, 
                                tmp, pdf >>

updateMonitor == /\ pc["master"] = "updateMonitor"
                 /\ IF (tmp < NUMBER_OF_WORKERS)
                       THEN /\ pdfMonitor' = Append(pdfMonitor, pdf_)
                            /\ tmp' = tmp + 1
                            /\ pc' = [pc EXCEPT !["master"] = "updateMonitor"]
                       ELSE /\ tmp' = 0
                            /\ pc' = [pc EXCEPT !["master"] = "Produce"]
                            /\ UNCHANGED pdfMonitor
                 /\ UNCHANGED << stateMonitor, pdf_number, pdf_, pdf >>

master == Produce \/ getPdf \/ putInMonitor \/ updateMonitor

Consume(self) == /\ pc[self] = "Consume"
                 /\ IF stateMonitor = "on"
                       THEN /\ pc' = [pc EXCEPT ![self] = "getFromMonitor"]
                       ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                 /\ UNCHANGED << pdfMonitor, stateMonitor, pdf_number, pdf_, 
                                 tmp, pdf >>

getFromMonitor(self) == /\ pc[self] = "getFromMonitor"
                        /\ pdfMonitor /= <<>> \/ pdf_number = TOTAL_PDF
                        /\ IF pdfMonitor /= <<>>
                              THEN /\ pdf' = [pdf EXCEPT ![self] = Head(pdfMonitor)]
                                   /\ pdfMonitor' = Tail(pdfMonitor)
                                   /\ IF pdfMonitor' = <<>> /\ pdf_number = TOTAL_PDF
                                         THEN /\ stateMonitor' = "off"
                                         ELSE /\ TRUE
                                              /\ UNCHANGED stateMonitor
                                   /\ pc' = [pc EXCEPT ![self] = "processPdf"]
                              ELSE /\ pc' = [pc EXCEPT ![self] = "Consume"]
                                   /\ UNCHANGED << pdfMonitor, stateMonitor, 
                                                   pdf >>
                        /\ UNCHANGED << pdf_number, pdf_, tmp >>

processPdf(self) == /\ pc[self] = "processPdf"
                    /\ PrintT(pdf[self])
                    /\ pc' = [pc EXCEPT ![self] = "Consume"]
                    /\ UNCHANGED << pdfMonitor, stateMonitor, pdf_number, pdf_, 
                                    tmp, pdf >>

worker(self) == Consume(self) \/ getFromMonitor(self) \/ processPdf(self)

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == master
           \/ (\E self \in {"worker1", "worker2"}: worker(self))
           \/ Terminating

Spec == /\ Init /\ [][Next]_vars
        /\ WF_vars(master)
        /\ \A self \in {"worker1", "worker2"} : WF_vars(worker(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION 
====
