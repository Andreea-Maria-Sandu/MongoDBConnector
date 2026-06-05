/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */

import { useState, useEffect,  useContext } from "react";
import { AppContext } from "../../Context/Context";
import JDBCType from "./JDBCType";
import KafkaType from "./KafkaType";
import WebserviceType from "./WebserviceType";
import SmallworldType from "./SmallworldType";
import CustomSwitch from "../DataViewComponents/CustomSwitch";

function SourcesDialog({ exchangeConnector, setExchangeConnector, connectorTypes, editMode, setIsContentValid, isContentValid, setIsDialogValid}) {

	const {state: { activeProject, connectors }, generalUseFunctions: { isValidValue, validateNameInput } } = useContext(AppContext);
	
	
	const [ connCategory, setConnCategory ] = useState("");
	
	const extractConnCategory = (type) => {
		if (!type) return "";
		const index = type.indexOf(":");
		return index === -1 ? type : type.substring(0, index);
	};

	useEffect(() => {
		const category = extractConnCategory(exchangeConnector.type);
		setConnCategory(category);
		setIsContentValid(false);
		if (editMode == false) 
			setExchangeConnector(prev => ({
				...prev,
				parametersContainer: {
					...prev.parametersContainer,
					development: {},
				}
			}));
		
	}, [ exchangeConnector.type ]);

	useEffect(() => {
		if (!isInvalidName() && isContentValid) setIsDialogValid(true);
		else setIsDialogValid(false);
		
	}, [ isContentValid, exchangeConnector.name ]);
	
	
	const isInvalidName = () => {
		if (editMode == true) {
			return false; //No validation for editMode
		}
		if (!isValidValue(exchangeConnector.name)) {
			
			return true;
		}
		const rec = connectors?.find(connector => 
			connector.projectId === activeProject._id && connector.name === exchangeConnector.name);
		if (isValidValue(rec)) {
			
			return true;
		}
		// Check for reserved keywords
		const containsKeywords = exchangeConnector.name.trim().toLowerCase().match(/(^_agg_$)/);
		if (containsKeywords) {
			return true;
		}
		const validationResult = validateNameInput(exchangeConnector.name);
		if(!validationResult.isValid)
			return true;
		
		return false;
	};
	
	const isInvalidType = () => {
		
		return !isValidValue(exchangeConnector.type);
		
		
	};
	
	const { disabled } = exchangeConnector;
	const switchLabel = disabled ? "Disabled" : "Enabled";
	

	
	
	//TO DO: pgschema for PostgeSQL
	const renderConnectorTypeComponent = () => {
		const jdbcTypes = [ "Oracle", "MySQL", "PostgreSQL", "MSSQL", "SAPHana", "MariaDB", "Mongo" ]; // list of JDBC types

		if (jdbcTypes.includes(connCategory)) {
			return (
				<JDBCType exchangeConnector={exchangeConnector} setExchangeConnector={setExchangeConnector} setIsContentValid={setIsContentValid} editMode={editMode} category={connCategory} />
			);
		}
		switch (connCategory) {
		case "Kafka":
			return <KafkaType data={exchangeConnector}	setData={setExchangeConnector} setIsContentValid={setIsContentValid} dataShape={exchangeConnector.parametersContainer.development} usedBy={"connector"}/>;
		
		case "Smallworld":
			return <SmallworldType exchangeConnector={exchangeConnector} setExchangeConnector={setExchangeConnector} setIsContentValid={setIsContentValid} />;
		case "WebService":
			return <WebserviceType exchangeConnector={exchangeConnector} setExchangeConnector={setExchangeConnector} dataShape={exchangeConnector.parametersContainer.development} setIsContentValid={setIsContentValid} editMode={editMode} />;
		default:
			return null;
		}
	};
	
	return (
		<>
			
			<div className="connector-state-switch-container">
				{editMode&&
				(<CustomSwitch
					label={exchangeConnector.disabled ? "Disabled" : "Enabled"} 
					enabled={!exchangeConnector.disabled}
					onClick={() => setExchangeConnector({ ...exchangeConnector, disabled: !exchangeConnector.disabled })} 
				></CustomSwitch>)}
			</div>
			<div className="row-container">
				
				<div className="choice-input row-cell2 margin1">
					<select
						id="newConnectorType"
						value={exchangeConnector?.type || ""}
						onChange={(e) => setExchangeConnector({ ...exchangeConnector, type: e.target.value })}
						disabled={!!exchangeConnector._id}
					>
						<option disabled value="">-- select a type --</option>
						{connectorTypes?.map((val) => (
							<option key={val.type} value={val.type}>
								{val.type}
							</option>
						))}
					</select>
					<label className={isInvalidType() ? "item-with-error" : ""}>TYPE</label>
				</div>

				<div className="data-input row-cell2 margin1">
					<input 
						title={"selectedProject.name"}
						type="text"
						id="newConnectorName"
						className="addConnEntity_connParam"
						maxLength={64}
						placeholder="name"
						disabled={!!exchangeConnector._id}
						onChange={(e) => setExchangeConnector({ ...exchangeConnector, name: e.target.value })}
						value={exchangeConnector.name|| ""}							
					/>
					<label className={isInvalidName() ? "item-with-error" : ""}>CONNECTOR NAME</label>
				</div>
			</div>
			{exchangeConnector.type && (
				<>
					<div className="row-container">
						<div className="data-input row-cell2 margin1">
							<input 
								onChange={(e) => setExchangeConnector({ ...exchangeConnector, replicaCount: e.target.value })} 
								id="connectorReplicaCount" 
								value={exchangeConnector.replicaCount|| 1} 
								type="number" min="1" max="8" 
								className="addConnEntity_connParam">
							</input>
							<label>Startup Replicas</label>
						</div>
						<div className="choice-input row-cell2 margin1">
							<select 
								onChange={(e) => setExchangeConnector({ ...exchangeConnector, cpuShares: e.target.value })}
								id="newConnectorCpuSharesEdit" 
								value={exchangeConnector.cpuShares|| 128}
							>
								{[ 128, 256, 384, 512, 640, 768, 896, 1024 ].map((val) => (
									<option key={val} value={val}>{val}</option>
								))}
							</select>
							<label>CPU Shares</label>
						</div>
					</div>
					{renderConnectorTypeComponent()}
					
				</>
			)}
		</>
	);
}

export default SourcesDialog;

