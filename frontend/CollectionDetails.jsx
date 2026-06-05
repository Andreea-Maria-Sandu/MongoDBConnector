/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable react/react-in-jsx-scope */
import { useContext, useEffect, useState } from "react";
import { AppContext } from "../../../Context/Context";
import { externalResources } from "../../../Imports/imports_externals";
import { visualElements } from "../../../Imports/imports_svg";
import "./CollectionDetails.css";
import "../../../Stylesheets/Input.css";
import DataView from "../../DataViewComponents/DataView";
import FieldsView from "../../DataViewComponents/FieldsView";
import FieldItem from "../../DataViewComponents/FieldItem";
import ResourceDetails from "../ResourceDetails";
import Dialog from "../../Dialog/Dialog";
import Predicate from "../../Predicate/Predicate";
import ConfirmationDialog from "../../ConfirmationDialog/ConfirmationDialog";

//TO DO: change field name fucntionality
function CollectionDetails({isMapped, isFieldAvailable, save, calculateFields, changeFieldName, getFieldChangedName}) {

	const { state: { activeResource, activeConnector, resourceDetails, activeProject, projectResources }, apiMongo: { deleteData, upsert, update, find } } = useContext(AppContext);
	
	const [ fields, setFields ] = useState([]);
	const [ activeField, setActiveField ] = useState(null);
	const [ showOnlyNativeIds, setShowOnlyNativeIds ] = useState(true);
	const [ showOnlyCustomNames, setShowOnlyCustomNames ] = useState(false);
	// const [ displayInfoFieldSources, setDisplayInfoFieldSources ] = useState(false);
	const [ showPredicateDialog, setShowPredicateDialog ] = useState(false);
	const [ canSavePredicate, setCanSavePredicate ] = useState(false);
	const [ predicateObjToSave, setPredicateObjToSave ] = useState({});
	const [ showConfirmationDialog, setShowConfirmationDialog ] = useState(false);
	const [ confirmationDialogMsg, setConfirmationDialogMsg ] = useState({});
	const [ onConfirmAction, setOnConfirmAction ] = useState(null);
	///Needed later for functionality///
	
	
	const [ changedCollectionField, setChangedCollectionField ] = useState(0);
	const [ fieldsFilter, setFieldsFilter ] = useState("");
	const [ confirmationModalMode, setConfirmationModalMode ] = useState(false);
	const [ dialogMode, setDialogMode ] = useState(false);
	const [ aggregateUsed, setAggregateUsed ] = useState("");
	const [ insertPredicateMode, setInsertPredicateMode ] = useState(false);
	
	
	
	useEffect(() => {
		if (resourceDetails ) {
			setFields(resourceDetails.fields?.filter((field) => {
				field.changedName = getFieldChangedName(field);
				
				return isFieldAvailable(field); 
				
			}));
			
		}
	}, [ resourceDetails ]);

	const toggleNativeIds = () => {
		if (showOnlyNativeIds) {
			if (!showOnlyCustomNames)
				return;
			setShowOnlyNativeIds(false);
		} else
			setShowOnlyNativeIds(true);

	};

	const toggleCustomNames = () => {
		if (showOnlyCustomNames){
			if (!showOnlyNativeIds)
				return;
			setShowOnlyCustomNames(false);
		} else
			setShowOnlyCustomNames(true);
	
	};
	
	const toggleField = (field) => {
		
		if (!isMapped(field)) 
			save(calculateFields( [ field.nativeIdentifier ]));
		else {
			// setDisplayInfoFieldSources(true);
			setActiveField(field);

			
		}
	};

	const removeField = async(field) => {
		if (isMapped(field)) {
			if (activeField && activeField.nativeIdentifier === field.nativeIdentifier) {
				field.changedName = null;
				closeInfoField();
			}
			const Aggregates = await find("AGGREGATE", { projectId: activeProject._id });
			const foundAggregates = Aggregates?.data?.filter(agg =>
				agg.sources?.some(source => source.connectorName === activeConnector.name && source.nativeIdentifier === activeResource.nativeIdentifier &&
				source.fields?.[field.nativeIdentifier] !== undefined
				)
			)|| [];
			// console.log("aaaaaaaaaaaaaaaag", foundAggregates);
			const aggregatesNames = foundAggregates?.map(agg => agg.name).join(", ");
			if (foundAggregates.length > 0) {
				setConfirmationDialogMsg(msgCannotRemoveField(field, aggregatesNames));
				setOnConfirmAction(null); // Informational, no action on accept
				setShowConfirmationDialog(true);
			} else if (isMongo && field.nativeIdentifier === "_id") {
				save([]);
			} else {
				save(calculateFields( [ field.nativeIdentifier ]));
			}

		}
	};
	
	const msgCannotRemoveField = (field, agg) => ({
		titleMsg: "Cannot Remove Field",
		spanBodyMsg: `Field: ${field?.nativeIdentifier}`,
		paragraphBodyMsg: `This Field is being used by Aggregates: ${agg}. Please remove it from Aggregate first before removing it from the Connector.`
	});

	const handleCloseConfirmationDialog = () => {
		setShowConfirmationDialog(false);
		setConfirmationDialogMsg({});
		setOnConfirmAction(null);
		
	};

	const closeInfoField = () => {
		// setDisplayInfoFieldSources(false);
		setActiveField(null);
	};

	const selectAll = () => {
		const allFields = fields;
		const mappedFields = allFields.filter(field => !isMapped(field)).map(field => field.nativeIdentifier);
		save( calculateFields(mappedFields) );
	};
	
	const deselectAll = async () => {
		
		save( [] );
		
	};
	



	
	
	
	const upsertPredicate = () => {
		const rec ={};
		rec.predicate = predicateObjToSave;
		upsert( "COLLECTION_DETAILS", { connectorId: activeConnector._id, nativeIdentifier: resourceDetails.nativeIdentifier }, rec );
		setShowPredicateDialog(false);
	};

	const removePredicate = () => {
		const rec ={};
		rec.predicate = null;
		update( "COLLECTION_DETAILS", { connectorId: activeConnector._id, nativeIdentifier: resourceDetails.nativeIdentifier }, rec );
	};



	const isMongo = activeConnector?.type?.startsWith("Mongo");

	const renderValidationSchema = () => {
		const schema = resourceDetails?.details?.validationSchema;
		if (!isMongo || schema == null) return null;

		let formatted = schema;
		try {
			formatted = JSON.stringify(JSON.parse(schema), null, 2);
		} catch {
			// not valid JSON — display as-is
		}

		const isValid = resourceDetails?.details?.valid;
		return (
			<DataView labelText="Validation Schema">
				<span>
					<button className={`schema-validity-badge ${isValid ? "schema-validity-valid" : "schema-validity-invalid"}`}>
						{isValid ? "Supported" : "Unsupported"}
					</button>
					<pre className="validation-schema-content">{formatted}</pre>
				</span>
			</DataView>
		);
	};

	if(resourceDetails)
		return (
			<>
				
				<DataView labelText="Native Name">
					<span> {resourceDetails.nativeIdentifier} </span>
					
				</DataView>
    
				<DataView labelText="Fields">
					<FieldsView
						fields={fields}
						showFilterButtons={true}
						showOnlyNativeIds={showOnlyNativeIds}
						showOnlyCustomNames={showOnlyCustomNames}
						showDeleteButton={true}
						showSelectButtons={true}
						toggleNativeIds={toggleNativeIds}
						toggleCustomNames={toggleCustomNames}
						selectAll={selectAll}
						deselectAll={deselectAll}
					>
						{/* FieldItem components rendered as children */}
						{fields && fields.map(field => (
							<FieldItem
								key={field.nativeIdentifier}
								field={field}
								isMapped={isMapped}
								// activeField={activeField}
								// displayInfoFieldSources={true}
								toggleField={toggleField}
								removeField={removeField}
								closeInfoField={closeInfoField}
								changeFieldName={changeFieldName}
								isActive= {field.nativeIdentifier === activeField?.nativeIdentifier}
								isFieldExpanded={field.nativeIdentifier === activeField?.nativeIdentifier}
								showDeleteButton={true}
								showOnlyNativeIds={showOnlyNativeIds} 
								showOnlyCustomNames={showOnlyCustomNames}
							/>
						))}
					</FieldsView>
				</DataView>
				{renderValidationSchema()}
				{showConfirmationDialog && (
					<ConfirmationDialog 
						onClose={handleCloseConfirmationDialog} 
						onAccept={handleCloseConfirmationDialog} 
						msg={confirmationDialogMsg}
						showActionButton={!!onConfirmAction} 
						cancelLabel={onConfirmAction ? "Cancel" : "Close"} 
						confirmLabel={onConfirmAction ? "Delete" : ""} 
					/>
				)}
				<div className="filter-btn-label">Filters</div>
				<div className="filter-btns-wrapper">
					<button type="button" className="predicate-filter-btn" onClick={() => setShowPredicateDialog(true) } title="Add Filter" style={{ display: !resourceDetails.predicate  ? "block" : "none"}}>
						<img src={visualElements.btnAddFilter} alt="Add Filter" />
					</button>
					<button type="button" className="predicate-filter-btn" onClick={() => setShowPredicateDialog(true)} title="Edit Filter" style={{ display: resourceDetails.predicate  ? "block" : "none" }}>
						<img src={visualElements.btnEditFilter} alt="Edit Filter" />
					</button>
					<button type="button" className="predicate-filter-btn" onClick={removePredicate} title="Remove Filter" style={{ display: resourceDetails.predicate  ? "block" : "none" }}>
						<img src={visualElements.btnDeleteFilter} alt="Remove Filter" />
					</button>
				</div>
				{showPredicateDialog && (
					<Dialog
						onClose={() => setShowPredicateDialog(false)}
						headerTitle={!resourceDetails.predicate  ? "INSERT PREDICATE" : "EDIT PREDICATE"}
						showButton={true}
						buttonText= "SAVE"
						dialogType="projectConnector"
						onButtonClick={upsertPredicate}
						isDialogValid={canSavePredicate}
					>
						<Predicate fields={fields} predicate={resourceDetails.predicate} setCanSavePredicate={setCanSavePredicate} setPredicateToSave={setPredicateObjToSave} isRelationship={false} />
					</Dialog>
				)}
			</>
		);
}

export default CollectionDetails;



// useEffect(() => {
// 	if ( details ) {
// 		const filteredFields = details.fields;
// 		console.log(filteredFields);
// 		// if (fieldsFilter) {
// 		// 	filteredFields = details.fields.filter(elem =>
// 		// 		elem.nativeIdentifier.toUpperCase().includes(fieldsFilter.toUpperCase()) || elem.name.toUpperCase().includes(fieldsFilter.toUpperCase())
// 		// 	);
// 		// }
// 		setFields(filteredFields);
// 	}
// }, [ details, fieldsFilter ]);
	
	
	
	
