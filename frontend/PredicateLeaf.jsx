/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useState, useEffect } from "react";



const WILDCARD = "__wildcard__";

const PredicateLeaf = ({ objectPred, setPredicateLeaf, fields, relationshipFields, setCanSavePredicate, isRelationship }) => {

	const operatorTypeEnum = [
		"AND", "OR", "EQUALS", "LIKE", "GREATER", "LOWER", "GREATER_EQ", "LOWER_EQ"
	];

	const fieldNames = fields.map(f => f.name);
	const isUnknownField = objectPred?.attributeName !== undefined &&
		objectPred.attributeName !== "" &&
		!fieldNames.includes(objectPred.attributeName);

	const [ wildcardMode, setWildcardMode ] = useState(isUnknownField);
	const showWildcard = wildcardMode || isUnknownField;
	const selectValue = showWildcard ? WILDCARD : (objectPred?.attributeName || "");

	useEffect(() => {
		const attributeName = objectPred?.attributeName;
		const attributeValue = objectPred?.attributeValue;
		if(isRelationship) setCanSavePredicate( attributeName&&attributeValue );
		else setCanSavePredicate(!!attributeName);
	}, [ objectPred.attributeName, objectPred.attributeValue ]);

	const handleChange = (e) => {
		const { name, value, type, checked } = e.target;
		const newValue = type === "checkbox" ? checked : value;

		setPredicateLeaf(prevState => ({
			...prevState,
			[name]: newValue
		}));
	};

	const handleFieldSelectChange = (e) => {
		const isWildcard = e.target.value === WILDCARD;
		setWildcardMode(isWildcard);
		setPredicateLeaf(prevState => ({ ...prevState, attributeName: isWildcard ? "" : e.target.value }));
	};

	const handleWildcardInput = (e) => {
		setPredicateLeaf(prevState => ({ ...prevState, attributeName: e.target.value }));
	};
	
	
	

	return(
		<div className="predicateCard">
			<div className="predicate-card-row">
				<div className="predicateSelect">
					<label className="operation-type">Operation:</label>
					<select
						name="operatorName" 
						value={objectPred?.operatorName||"EQUALS"}
						onChange={handleChange}
						style={{ width: "100px" }}
						disabled={isRelationship}
						
					>
						{operatorTypeEnum.map(opt => (
							<option key={opt} value={opt}>{opt}</option>
						))}
					</select>
				</div>
				<div className="predicate-switch-container">
					<label>
                  Negated:
						<input 
							name="negated"
							type="checkbox" 
                            
							checked={objectPred?.negated||false}
							onChange={handleChange}
						/>
					</label>
				</div>
			</div>
			
			<div className="predicate-card-row">
				<div className="predicateSelect">
					<label htmlFor="attributeName" className="operation-type">{isRelationship ? "Child Field" : "Field"}:</label>
					<select
						value={selectValue}
						name="attributeName"
						onChange={handleFieldSelectChange}
						title={showWildcard ? "Wildcard" : undefined}
						style={{ width: showWildcard ? "36px" : "100px", flexShrink: 0 }}
					>
						<option value="">--Select--</option>
						{fields.map((field) => (
							<option key={field.nativeIdentifier} value={field.name}>
								{field.name}
							</option>
						))}
						<option value={WILDCARD} title="Wildcard">*</option>
					</select>
					{showWildcard && (
						<input
							type="text"
							value={objectPred?.attributeName || ""}
							onChange={handleWildcardInput}
							placeholder="field name"
							style={{ width: "100px", marginLeft: "2px" }}
						/>
					)}
				</div>
				<div className="predicate-switch-container">
					<label htmlFor="attributeValue">{isRelationship ? "Parent Field" : "Value"}:</label>
					{isRelationship ? (
						<select
							id="attributeValue"
							value={objectPred?.attributeValue || ""}
							name="attributeValue"
							onChange={handleChange}
							style={{ width: "100px" }}
						>
							<option value="">--Select--</option>
							{relationshipFields.map((field, index) => (
								<option key={index} value={field}>
									{field}
								</option>
							))}
						</select>
					) : (
						<input
							id="attributeValue"
							type="text"
							name="attributeValue"
							value={objectPred?.attributeValue || ""}
							onChange={handleChange}
							placeholder="value"
						/>
					)}
					
				</div>
			</div>
			
		</div>
	);
};

export default PredicateLeaf;