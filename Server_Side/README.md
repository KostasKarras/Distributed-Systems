# Distributed-Systems

Αναστασόπουλος Δημήτριος - 3180010
Δικαιόπουλος Μιχαήλ - 3180050
Καρράς Κωνσταντίνος - 3180076

Για να τρέξει σωστά το Project, απαιτείται η εκτέλεση κάθε νέας διεργασίας
σε διαφορετικό μηχάνημα ή σε περιβάλλον Docker.

Σειρά εκτέλεσης διεργασιών :
1. Address Keeper - 1
2. Brokers - Όσοι απαιτούνται
3. App Nodes - Όσοι απαιτούνται

Εκτέλεση σε διαφορετικά μηχανήματα :
1. Κατεβάζουμε το project σε κάθε μηχάνημα που θα το εκτελέσουμε
2. Κάνουμε εξαγωγή των αρχείων του zip σε όποιο φάκελο θέλουμε
3. Ανοίγουμε το terminal και θέτουμε current directory τον φάκελο που είναι 
    αποθηκευμένο το project (3180010_3180050_3180076).

3.  Για τον Address Keeper :
	- cd out\artifacts\AddressKeeper_jar
	- java -jar Distributed_systems.jar

4. Για τους Brokers :
	- cd out\artifacts\Broker_jar
	- java -jar Distributed_systems.jar
	- Στο πεδίο Give Address Keeper IP address δίνουμε την διεύθυνση
	  IP που έχει εμφανιστεί στο terminal του Address Keeper.

5. Για τους App Nodes :
	- cd out\artifacts\AppNode_jar
	-java -jar Distributed_systems.jar
	- Στο πεδίο Give Address Keeper IP address δίνουμε την διεύθυνση
	  IP που έχει εμφανιστεί στο terminal του Address Keeper.
	- Στο πεδίο com.example.uni_tok.Channel Name δίνουμε το όνομα του καναλιού του
	  συγκεκριμένου App com.example.uni_tok.Node.

