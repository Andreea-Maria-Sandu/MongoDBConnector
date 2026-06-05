/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useState, useEffect } from "react";
import "./Predicate.css";
import "../Dialog/Dialog.css";
import PredicateEditor from "./PredicateEditor";


const Predicate = ({ fields, predicate, setCanSavePredicate, setPredicateToSave, isRelationship, relationshipFields }) => {
	
	
	const [ changedAggr, setChangedAggr ] = useState(0); 

	const [ predicateObject, setPredicateObject ] = useState({});

	
	
	useEffect(() => {
		// console.log("predicate ready to save ", predicateObject);
		setPredicateToSave(predicateObject);
	}, [ predicateObject, setPredicateToSave ]);
	
	useEffect(() => {
		if (predicate && Object.keys(predicate).length > 0) {
			setPredicateObject(predicate);
			// console.log("initial predicate set predicate.jsx", predicate);
		} else {
		// Initialize with default structure if predicate is empty
			setPredicateObject({
				operatorName: "EQUALS",
				negated: false,
				attributeName: "",
				attributeValue: ""
			});
		}
	}, [ predicate ]);
	



	return (
		<div id="predicateTemplateWrapper">
			<div className="predicate-centered-div">
				<PredicateEditor objectPred={predicateObject} setPredicateObject={setPredicateObject} fields={fields} setCanSavePredicate={setCanSavePredicate} isRelationship={isRelationship} relationshipFields={relationshipFields} />
				
			</div>
			
		</div>
	);
};

export default Predicate;