--------------------------- MODULE step5_porject ---------------------------

EXTENDS TLC, Integers, Sequences

(*--algorithm pcd_assignment
variable pdfMonitor = <<>>,
tmp = 0;

process master = "master"
variable pdf = "",
pdf_number=0;
begin Master:
  while TRUE do
    getPdf:
        pdf_number := pdf_number+1;
        pdf := "pdf_"+pdf_number;
    putInMonitor: 
        await pdfMonitor = <<>>;
        updateMonitor:
            while(tmp < N_WORKERS) do
                pdfMonitor := Append(pdfMonitor, pdf);
                tmp := tmp + 1
            end while;
        tmp := 0
  end while;
end process;

process worker \in {"worker1", "worker2", "worker3", "worker4", "worker5"}
variable pdf = "none";
begin Worker:
  while TRUE do
    getFromMonitor: 
        await pdfMonitor /= <<>>;
        pdf := Head(pdfMonitor);
        pdfMonitor := Tail(pdfMonitor);
    processPdf:
        print pdf;
  end while;
end process;
end algorithm;*)
================================================================
\* BEGIN TRANSLATION (chksum(pcal) = "6c87fdbf" /\ chksum(tla) = "a0fd341")
\* Process variable pdf of process master at line 10 col 10 changed to pdf_
VARIABLES pdfMonitor, tmp, pc, pdf_, pdf_number, pdf

vars == << pdfMonitor, tmp, pc, pdf_, pdf_number, pdf >>

ProcSet == {"master"} \cup ({"worker1", "worker2", "worker3", "worker4", "worker5"})

Init == (* Global variables *)
        /\ pdfMonitor = <<>>
        /\ tmp = 0
        (* Process master *)
        /\ pdf_ = ""
        /\ pdf_number = 0
        (* Process worker *)
        /\ pdf = [self \in {"worker1", "worker2", "worker3", "worker4", "worker5"} |-> "none"]
        /\ pc = [self \in ProcSet |-> CASE self = "master" -> "Master"
                                        [] self \in {"worker1", "worker2", "worker3", "worker4", "worker5"} -> "Worker"]

Master == /\ pc["master"] = "Master"
          /\ pc' = [pc EXCEPT !["master"] = "getPdf"]
          /\ UNCHANGED << pdfMonitor, tmp, pdf_, pdf_number, pdf >>

getPdf == /\ pc["master"] = "getPdf"
          /\ pdf_number' = pdf_number+1
          /\ pdf_' = "pdf_"+pdf_number'
          /\ pc' = [pc EXCEPT !["master"] = "putInMonitor"]
          /\ UNCHANGED << pdfMonitor, tmp, pdf >>

putInMonitor == /\ pc["master"] = "putInMonitor"
                /\ pdfMonitor = <<>>
                /\ pc' = [pc EXCEPT !["master"] = "updateMonitor"]
                /\ UNCHANGED << pdfMonitor, tmp, pdf_, pdf_number, pdf >>

updateMonitor == /\ pc["master"] = "updateMonitor"
                 /\ IF (tmp < N_WORKERS)
                       THEN /\ pdfMonitor' = Append(pdfMonitor, pdf_)
                            /\ tmp' = tmp + 1
                            /\ pc' = [pc EXCEPT !["master"] = "updateMonitor"]
                       ELSE /\ tmp' = 0
                            /\ pc' = [pc EXCEPT !["master"] = "Master"]
                            /\ UNCHANGED pdfMonitor
                 /\ UNCHANGED << pdf_, pdf_number, pdf >>

master == Master \/ getPdf \/ putInMonitor \/ updateMonitor

Worker(self) == /\ pc[self] = "Worker"
                /\ pc' = [pc EXCEPT ![self] = "getFromMonitor"]
                /\ UNCHANGED << pdfMonitor, tmp, pdf_, pdf_number, pdf >>

getFromMonitor(self) == /\ pc[self] = "getFromMonitor"
                        /\ pdfMonitor /= <<>>
                        /\ pdf' = [pdf EXCEPT ![self] = Head(pdfMonitor)]
                        /\ pdfMonitor' = Tail(pdfMonitor)
                        /\ pc' = [pc EXCEPT ![self] = "processPdf"]
                        /\ UNCHANGED << tmp, pdf_, pdf_number >>

processPdf(self) == /\ pc[self] = "processPdf"
                    /\ PrintT(pdf[self])
                    /\ pc' = [pc EXCEPT ![self] = "Worker"]
                    /\ UNCHANGED << pdfMonitor, tmp, pdf_, pdf_number, pdf >>

worker(self) == Worker(self) \/ getFromMonitor(self) \/ processPdf(self)

Next == master
           \/ (\E self \in {"worker1", "worker2", "worker3", "worker4", "worker5"}: worker(self))

Spec == Init /\ [][Next]_vars

\* END TRANSLATION 
